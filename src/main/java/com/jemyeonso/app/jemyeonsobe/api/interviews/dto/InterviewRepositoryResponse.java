package com.jemyeonso.app.jemyeonsobe.api.interviews.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class InterviewRepositoryResponse {
    private List<InterviewListResponse> interviews;

    // 페이지네이션 정보 추가
    private int page;              // 전체 데이터 개수
    private int totalPages;             // 마지막 페이지 여부

    // 기존 생성자 (하위 호환성)
    public InterviewRepositoryResponse(List<InterviewListResponse> interviews) {
        this.interviews = interviews;
        this.page = 0;
        this.totalPages = 1;
    }

    // Page 객체로부터 생성하는 정적 메서드
    public static InterviewRepositoryResponse from(Page<InterviewListResponse> page) {
        return InterviewRepositoryResponse.builder()
                .interviews(page.getContent())
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .build();
    }
}
