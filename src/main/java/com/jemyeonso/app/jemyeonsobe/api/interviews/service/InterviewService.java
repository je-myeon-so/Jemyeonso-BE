package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.*;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Answer;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.AnswerRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiAnalysisService;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiQuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.dto.AiQuestionResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionService;
import com.jemyeonso.app.jemyeonsobe.api.user.service.UserDetailService;
import com.jemyeonso.app.jemyeonsobe.api.user.service.UserService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ErrorMessage;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import com.jemyeonso.app.jemyeonsobe.common.exception.InterviewAccessDeniedException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;
    private final AiQuestionService aiQuestionService;
    private final AiAnalysisService aiAnalysisService;
    private final AnswerRepository answerRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserDetailService userDetailService;

    private String ptrKey(Long userId) {
        return "user:" + userId + ":improvement:latest:interviewId";
    }

    private String lockKey(Long u, Long i){
        return "lock:improve:user:" + u + ":interview:" + i;
    }

    private boolean tryLock(String key, Duration ttl) {
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(ok);
    }

    @Async("improvementExecutor")
    public void refreshImprovementAsync(Long userId, Long interviewId, Long ignored1, String ignored2) {
        try {
            userDetailService.refreshImprovementFromAi(userId, interviewId);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(getClass())
                .warn("improvement refresh failed: userId={}, interviewId={}, err={}",
                    userId, interviewId, e.toString());
        }
    }

    @Transactional
    public void finishInterview(Long userId, Long interviewId) {
        // 인터뷰 권한
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.INTERVIEW_NOT_FOUND));

        if (!interview.getUserId().equals(userId)) {
            throw new InterviewAccessDeniedException(ErrorMessage.NO_INTERVIEW_PERMISSION);
        }

        // 중복 트리거 방지: 최신 포인터 확인 + 락 시도
        String ptr = redisTemplate.opsForValue().get(ptrKey(userId));
        boolean need = (ptr == null || !ptr.equals(String.valueOf(interviewId)));
        String lock = lockKey(userId, interviewId);

        if (!need) {
            // 이미 해당 인터뷰로 갱신된거면 그냥 리턴
            return;
        }

        // 커밋 이후 비동기로 갱신 실행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                if (tryLock(lock, Duration.ofMinutes(10))) {
                    try {
                        // 비동기 실행
                        refreshImprovementAsync(userId, interviewId, null, null);
                    } catch (Exception e) {
                        org.slf4j.LoggerFactory.getLogger(getClass())
                            .warn("finishInterview: async refresh failed userId={}, interviewId={}, err={}",
                                userId, interviewId, e.toString());
                    }
                }
            }
        });


    }

    @Transactional
    public InterviewResponseDto createInterview(Long userId, InterviewRequestDto requestDto) {
        Interview interview = Interview.builder()
                .questionCategory(Interview.QuestionCategory.valueOf(requestDto.getQuestionCategory()))
                .questionLevel(Interview.QuestionLevel.valueOf(requestDto.getQuestionLevel()))
                .jobtype(requestDto.getJobType())
                .documentId(requestDto.getDocumentId())
                .userId(userId)
                .title(requestDto.getInterviewTitle())
                .build();

        interviewRepository.save(interview);

        AiQuestionRequestDto aiRequest = new AiQuestionRequestDto(
                requestDto.getQuestionLevel(),
                requestDto.getJobType(),
                requestDto.getQuestionCategory(),
                null, // previousQuestion
                null, // previousAnswer
                requestDto.getDocumentId()
        );

        AiQuestionResponseDto aiQuestionResponseDto = aiQuestionService.requestAndSaveQuestion(interview.getId(), aiRequest);

        return new InterviewResponseDto(interview.getId(), aiQuestionResponseDto.getData().getQuestion(), aiQuestionResponseDto.getData().getQuestionId(), aiQuestionResponseDto.getData().getQuestionType());
    }

    @Transactional
    public InterviewResponseDto createQuestion(QuestionRequestDto requestDto, Long userId) {
        Interview interview = interviewRepository.findById(requestDto.getInterviewId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.INTERVIEW_NOT_FOUND));

        // 유저 검증: 면접 소유자와 요청한 유저가 다르면 접근 거부
        if (!interview.getUserId().equals(userId)) {
            throw new InterviewAccessDeniedException(ErrorMessage.NO_INTERVIEW_PERMISSION);
        }

        // 답변 저장
        Answer answer = Answer.builder()
                .answerTime(null)
                .content(requestDto.getPreviousAnswer())
                .questionId(requestDto.getPreviousQuestionId())
                .build();

        answerRepository.save(answer);

        aiAnalysisService.analyzeAnswerAsync(answer, interview, requestDto.getPreviousQuestion());

        // AI 요청 구성
        AiQuestionRequestDto aiRequest = new AiQuestionRequestDto(
                interview.getQuestionLevel().name(),
                interview.getJobtype(),
                interview.getQuestionCategory().name(),
                requestDto.getPreviousQuestion(),
                requestDto.getPreviousAnswer(),
                interview.getDocumentId()
        );

        AiQuestionResponseDto aiQuestionResponseDto = aiQuestionService.requestAndSaveQuestion(interview.getId(), aiRequest);
        return new InterviewResponseDto(interview.getId(), aiQuestionResponseDto.getData().getQuestion(), aiQuestionResponseDto.getData().getQuestionId(), aiQuestionResponseDto.getData().getQuestionType());
    }

    public InterviewRepositoryResponse getInterviewRepository(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Interview> interviewPage = interviewRepository.findAllWithDocument(pageable);

        List<InterviewListResponse> interviews = interviewPage.getContent().stream()
                .map(InterviewListResponse::from)
                .collect(Collectors.toList());

        Page<InterviewListResponse> interviewResponsePage = new PageImpl<>(
                interviews,
                pageable,
                interviewPage.getTotalElements()
        );

        return InterviewRepositoryResponse.from(interviewResponsePage);
    }

    // 기존 메서드들 (하위 호환성을 위해 유지)
    @Transactional
    public InterviewResponseDto createQuestion(QuestionRequestDto requestDto) {
        Interview interview = interviewRepository.findById(requestDto.getInterviewId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.INTERVIEW_NOT_FOUND));

        // AI 요청 구성
        AiQuestionRequestDto aiRequest = new AiQuestionRequestDto(
                interview.getQuestionLevel().name(),
                interview.getJobtype(),
                interview.getQuestionCategory().name(),
                requestDto.getPreviousQuestion(),
                requestDto.getPreviousAnswer(),
                interview.getDocumentId()
        );

        AiQuestionResponseDto aiQuestionResponseDto = aiQuestionService.requestAndSaveQuestion(interview.getId(), aiRequest);
        return new InterviewResponseDto(interview.getId(), aiQuestionResponseDto.getData().getQuestion(), aiQuestionResponseDto.getData().getQuestionId(), aiQuestionResponseDto.getData().getQuestionType());
    }

    public InterviewRepositoryResponse getInterviewRepository(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 현재 로그인한 유저의 면접만 조회
        Page<Interview> interviewPage = interviewRepository.findByUserIdWithDocument(userId, pageable);

        List<InterviewListResponse> interviews = interviewPage.getContent().stream()
                .map(InterviewListResponse::from)
                .collect(Collectors.toList());

        // Page<InterviewListResponse> 형태로 변환하여 from 메서드 사용
        Page<InterviewListResponse> interviewResponsePage = new PageImpl<>(
                interviews,
                pageable,
                interviewPage.getTotalElements()
        );

        return InterviewRepositoryResponse.from(interviewResponsePage);
    }

    public InterviewQuestionsResponseDto getInterviewQuestions(Long interviewId, Long userId) {
        // 면접 존재 여부 확인 - 기존 ResourceNotFoundException 활용
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.INTERVIEW_NOT_FOUND));

        // 권한 확인 - 기존 InterviewAccessDeniedException 활용
        if (!interview.getUserId().equals(userId)) {
            throw new InterviewAccessDeniedException(ErrorMessage.NO_INTERVIEW_PERMISSION);
        }

        // 질문 목록 조회
        List<Question> questions = questionRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId);

        List<InterviewQuestionsResponseDto.QuestionSummaryDto> questionSummaries = questions.stream()
                .map(question -> InterviewQuestionsResponseDto.QuestionSummaryDto.builder()
                        .questionId(question.getId())
                        .content(question.getContent())
                        .questionType(question.getQuestionType())
                        .createdAt(question.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 사용자의 최신 인터뷰만 대상으로
        interviewRepository.findLatestByUserId(userId).ifPresent(latest -> {
            if (latest.getId().equals(interviewId)) {
                // 포인터 확인
                String currentPtr = redisTemplate.opsForValue().get(ptrKey(userId));
                boolean need = (currentPtr == null || !currentPtr.equals(String.valueOf(interviewId)));

                if (need) {
                    // 중복 방지 락 (예: 10분)
                    Boolean got = redisTemplate.opsForValue().setIfAbsent(lockKey(userId, interviewId), "1", Duration.ofMinutes(10));
                    if (Boolean.TRUE.equals(got)) {
                        // 더블 체크: 경쟁 상황에서 이미 누가 갱신했을 수 있음
                        String again = redisTemplate.opsForValue().get(ptrKey(userId));
                        if (again == null || !again.equals(String.valueOf(interviewId))) {
                            // 비동기 갱신 추천 (응답 지연 방지)
                            refreshImprovementAsync(userId, latest.getId(), latest.getDocumentId(), latest.getJobtype());
                        }
                    }
                }
            }
        });

        return InterviewQuestionsResponseDto.builder()
                .interviewId(interview.getId())
                .documentId(interview.getDocumentId())
                .userId(interview.getUserId())
                .title(interview.getTitle())
                .questionCategory(interview.getQuestionCategory().name())
                .questionLevel(interview.getQuestionLevel().name())
                .jobtype(interview.getJobtype())
                .totalScore(interview.getAvgScore())
                .createdAt(interview.getCreatedAt())
                .questions(questionSummaries)
                .build();
    }

    public QuestionDetailResponseDto getQuestionDetail(Long interviewId, Long questionId, Long userId) {
        // 면접 존재 여부 및 권한 확인
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.INTERVIEW_NOT_FOUND));

        if (!interview.getUserId().equals(userId)) {
            throw new InterviewAccessDeniedException(ErrorMessage.NO_INTERVIEW_PERMISSION);
        }

        // 질문 존재 여부 및 면접 소속 확인
        Question question = questionRepository.findByIdAndInterviewId(questionId, interviewId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.QUESTION_NOT_FOUND));

        // 답변 조회 (피드백과 함께)
        Optional<Answer> answerOpt = answerRepository.findByQuestionIdWithFeedbacks(questionId);

        QuestionDetailResponseDto.AnswerDetailDto answerDetail = null;
        if (answerOpt.isPresent()) {
            Answer answer = answerOpt.get();

            List<QuestionDetailResponseDto.FeedbackDto> feedbackDtos = answer.getFeedbacks().stream()
                    .map(feedback -> QuestionDetailResponseDto.FeedbackDto.builder()
                            .feedbackId(feedback.getId())
                            .errorText(feedback.getErrorText())
                            .errorType(feedback.getErrorType())
                            .feedback(feedback.getFeedback())
                            .suggestion(feedback.getSuggestion())
                            .build())
                    .collect(Collectors.toList());

            answerDetail = QuestionDetailResponseDto.AnswerDetailDto.builder()
                    .answerId(answer.getId())
                    .content(answer.getContent())
                    .answerTime(answer.getAnswerTime())
                    .feedbacks(feedbackDtos)
                    .build();
        }

        return QuestionDetailResponseDto.builder()
                .questionId(question.getId())
                .interviewId(question.getInterviewId())
                .content(question.getContent())
                .questionType(question.getQuestionType())
                .createdAt(question.getCreatedAt())
                .answer(answerDetail)
                .build();
    }
}
