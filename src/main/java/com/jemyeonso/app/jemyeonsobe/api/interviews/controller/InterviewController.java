package com.jemyeonso.app.jemyeonsobe.api.interviews.controller;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.InterviewService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/backend/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @PostMapping
    public ResponseEntity<?> createInterview(@RequestBody InterviewRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewResponseDto interviewResponseDto = interviewService.createInterview(userId, requestDto);
        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.INTERVIEW_CREATE_SUCCESS, "인터뷰 생성에 성공하였습니다.", interviewResponseDto));
    }

    @GetMapping("/repository")
    public ResponseEntity<ApiResponse<InterviewRepositoryResponse>> getInterviewRepository(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ApiResponseCode.PAGINATION_INVALID_PARAMETER));
        }

        try {
            InterviewRepositoryResponse response = interviewService.getInterviewRepository(page, size);
            return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.INTERVIEW_LIST_GET_SUCCESS, response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
        }
    }
}