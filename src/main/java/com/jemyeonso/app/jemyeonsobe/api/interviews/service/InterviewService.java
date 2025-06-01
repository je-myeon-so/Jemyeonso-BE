package com.jemyeonso.app.jemyeonsobe.api.interviews.service;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewListResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.entity.Interview;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.InterviewRepository;
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

    public InterviewRepositoryResponse getInterviewRepository(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Interview> interviewPage = interviewRepository.findAllWithDocument(pageable);

        List<InterviewListResponse> interviews = interviewPage.getContent().stream()
                .map(InterviewListResponse::from)
                .collect(Collectors.toList());

        return new InterviewRepositoryResponse(interviews);
    }
}