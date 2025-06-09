package com.jemyeonso.app.jemyeonsobe.api.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DocumentRepositoryResponse {
    private List<DocumentResponse> documents;

    // 페이지네이션 정보
    private int page;               // 전체 데이터 개수
    private int totalPages;             // 마지막 페이지 여부

    // 기존 생성자 (하위 호환성)
    public DocumentRepositoryResponse(List<DocumentResponse> documents) {
        this.documents = documents;
        this.page = 0;
        this.totalPages = 1;
    }

    // Page 객체로부터 생성하는 정적 메서드
    public static DocumentRepositoryResponse from(Page<DocumentResponse> page) {
        return DocumentRepositoryResponse.builder()
                .documents(page.getContent())
                .page(page.getNumber())
                .totalPages(page.getTotalPages())
                .build();
    }
}
