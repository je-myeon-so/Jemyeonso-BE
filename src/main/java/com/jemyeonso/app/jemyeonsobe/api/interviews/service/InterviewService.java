package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.document.entity.Document;
import com.jemyeonso.app.jemyeonsobe.api.document.repository.DocumentRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.AnalysisDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.AnswerDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.FeedbackDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.FileDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewDetailsResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewListResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.QuestionDetailsDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.QuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Answer;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Feedback;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.AnswerRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.FeedbackRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionService;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
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
    private final DocumentRepository documentRepository;
    private final AnswerRepository answerRepository;
    private final FeedbackRepository feedbackRepository;
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
    public InterviewResponseDto createQuestion(QuestionRequestDto requestDto) {
        Interview interview = interviewRepository.findById(requestDto.getInterviewId())
            .orElseThrow(() -> new ResourceNotFoundException("해당 인터뷰가 존재하지 않습니다."));

        if (requestDto.getPreviousQuestionId() != null && requestDto.getPreviousAnswer() != null) {
            Question previousQuestion = questionRepository.findById(requestDto.getPreviousQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("이전 질문을 찾을 수 없습니다."));

            Answer answer = Answer.builder()
                .question(previousQuestion)
                .content(requestDto.getPreviousAnswer())
                .answerTime(requestDto.getAnswerTime())
                .score(null)
                .build();

            answerRepository.save(answer);
        }

        // AI 요청
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

    @Transactional(readOnly = true)
    public InterviewDetailsResponse getInterviewDetails(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
            .orElseThrow(() -> new ResourceNotFoundException("해당 인터뷰가 존재하지 않습니다."));

        List<Question> questions = questionRepository.findByInterviewId(interviewId);

        List<QuestionDetailsDto> questionDtos = new ArrayList<>();
        int questionNum = 1;

        for (Question question : questions) {
            Answer answer = question.getAnswer(); // 연관 관계에서 바로 가져오기

            List<Feedback> feedbacks = (answer != null)
                ? feedbackRepository.findByAnswerId(answer.getId())
                : Collections.emptyList();

            List<AnalysisDto> analysisList = feedbacks.stream()
                .map(fb -> new AnalysisDto(
                    fb.getErrorText(),
                    fb.getErrorType(),
                    fb.getFeedback(),
                    fb.getSuggestion()
                ))
                .toList();

            questionDtos.add(new QuestionDetailsDto(
                question.getId(),
                question.getContent(),
                questionNum++,
                "기본질문", // QuestionType 필드가 없으므로 임시 하드코딩
                (answer != null) ? new AnswerDto(answer.getId(), answer.getContent()) : null,
                feedbacks.isEmpty() ? null : new FeedbackDto(feedbacks.get(0).getId(), feedbacks.get(0).getFeedback()),
                analysisList
            ));
        }

        Document resume = documentRepository.findById(interview.getDocumentId())
            .orElseThrow(() -> new ResourceNotFoundException("연관된 이력서가 존재하지 않습니다."));

        return new InterviewDetailsResponse(
            interview.getId(),
            interview.getUserId(),
            new FileDto(resume.getId(), resume.getFilename(), resume.getLink()), // ✅ 수정된 부분
            interview.getQuestionCategory().name(),
            interview.getQuestionLevel().name(),
            interview.getJobtype(),
            interview.getCreatedAt(),
            questionDtos
        );
    }


    public InterviewRepositoryResponse getInterviewRepository(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Interview> interviewPage = interviewRepository.findAllWithDocument(pageable);

        List<InterviewListResponse> interviews = interviewPage.getContent().stream()
                .map(InterviewListResponse::from)
                .collect(Collectors.toList());

        return new InterviewRepositoryResponse(interviews);
    }
}