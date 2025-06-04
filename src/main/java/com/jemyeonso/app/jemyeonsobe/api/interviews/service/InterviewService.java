package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewListResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Question;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
            .documentId(requestDto.getFileId())
            .userId(userId)
            .title(requestDto.getInterviewTitle())
            .build();

        interviewRepository.save(interview);

        AiQuestionRequest aiRequest = new AiQuestionRequest(
            requestDto.getQuestionLevel(),
            requestDto.getJobType(),
            requestDto.getQuestionCategory(),
            null, // previousQuestion
            null, // previousAnswer
            requestDto.getFileId()
        );

        Question question = aiQuestionService.requestAndSaveQuestion(interview.getId(), aiRequest);

        return new InterviewResponseDto(interview.getId(), question.getContent());
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