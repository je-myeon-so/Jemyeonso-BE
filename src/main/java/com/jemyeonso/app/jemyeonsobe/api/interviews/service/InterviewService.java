package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewListResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.QuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.ai.AiQuestionService;
import com.jemyeonso.app.jemyeonsobe.common.exception.ResourceNotFoundException;
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
}