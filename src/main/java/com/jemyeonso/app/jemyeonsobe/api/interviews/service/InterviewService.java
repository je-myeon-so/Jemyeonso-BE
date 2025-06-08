package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.*;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionService;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import com.jemyeonso.app.jemyeonsobe.common.exception.InterviewAccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final QuestionRepository questionRepository;
    private final AiQuestionService aiQuestionService;

    @Transactional
    public InterviewResponseDto createInterview(Long userId, InterviewRequestDto requestDto) {
        Interview interview = Interview.builder()
                .questionCategory(Interview.QuestionType.valueOf(requestDto.getQuestionCategory()))
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

        return new InterviewResponseDto(interview.getId(), aiQuestionResponseDto.getData().getQuestion(), aiQuestionResponseDto.getData().getQuestionType());
    }

    @Transactional
    public InterviewResponseDto createQuestion(QuestionRequestDto requestDto, Long userId) {
        Interview interview = interviewRepository.findById(requestDto.getInterviewId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 인터뷰가 존재하지 않습니다."));

        // 유저 검증: 면접 소유자와 요청한 유저가 다르면 접근 거부
        if (!interview.getUserId().equals(userId)) {
            throw new InterviewAccessDeniedException("해당 면접에 접근할 권한이 없습니다.");
        }

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
        return new InterviewResponseDto(interview.getId(), aiQuestionResponseDto.getData().getQuestion(), aiQuestionResponseDto.getData().getQuestionType());
    }

    public InterviewRepositoryResponse getInterviewRepository(int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 현재 로그인한 유저의 면접만 조회
        Page<Interview> interviewPage = interviewRepository.findByUserIdWithDocument(userId, pageable);

        List<InterviewListResponse> interviews = interviewPage.getContent().stream()
                .map(InterviewListResponse::from)
                .collect(Collectors.toList());

        return new InterviewRepositoryResponse(interviews);
    }

    // 기존 메서드들 (하위 호환성을 위해 유지)
    @Transactional
    public InterviewResponseDto createQuestion(QuestionRequestDto requestDto) {
        Interview interview = interviewRepository.findById(requestDto.getInterviewId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 인터뷰가 존재하지 않습니다."));

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
        return new InterviewResponseDto(interview.getId(), aiQuestionResponseDto.getData().getQuestion(), aiQuestionResponseDto.getData().getQuestionType());
    }

    public InterviewRepositoryResponse getInterviewRepository(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Interview> interviewPage = interviewRepository.findAllWithDocument(pageable);

        List<InterviewListResponse> interviews = interviewPage.getContent().stream()
                .map(InterviewListResponse::from)
                .collect(Collectors.toList());

        return new InterviewRepositoryResponse(interviews);
    }

    public InterviewQuestionsResponseDto getInterviewQuestions(Long interviewId, Long userId) {
        // 면접 존재 여부 확인 - 기존 ResourceNotFoundException 활용
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 면접입니다."));

        // 권한 확인 - 기존 InterviewAccessDeniedException 활용
        if (!interview.getUserId().equals(userId)) {
            throw new InterviewAccessDeniedException("해당 면접에 접근할 권한이 없습니다.");
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

        return InterviewQuestionsResponseDto.builder()
                .interviewId(interview.getId())
                .documentId(interview.getDocumentId())
                .userId(interview.getUserId())
                .title(interview.getTitle())
                .questionCategory(interview.getQuestionCategory().name())
                .questionLevel(interview.getQuestionLevel().name())
                .jobtype(interview.getJobtype())
                .totalScore(interview.getTotalScore())
                .createdAt(interview.getCreatedAt())
                .questions(questionSummaries)
                .build();
    }
}
