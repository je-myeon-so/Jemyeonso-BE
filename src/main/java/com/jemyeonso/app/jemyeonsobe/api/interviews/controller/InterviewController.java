package com.jemyeonso.app.jemyeonsobe.api.interviews.controller;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewDetailsResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRepositoryResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewRequestDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.InterviewResponseDto;
import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.QuestionRequestDto;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.InterviewService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/backend/interviews")
@RequiredArgsConstructor
@Tag(name = "Interview", description = "인터뷰 질문 관련 API")
public class InterviewController {

    private final InterviewService interviewService;

    @Operation(
        summary = "면접 세션 생성",
        description = "이력서와 직무 정보를 기반으로 면접 세션을 생성하고 첫 번째 질문을 반환합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "면접 세션 생성 성공",
            content = @Content(schema = @Schema(implementation = InterviewResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping
    public ResponseEntity<?> createInterview(@RequestBody InterviewRequestDto requestDto) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewResponseDto interviewResponseDto = interviewService.createInterview(userId, requestDto);
        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.INTERVIEW_CREATE_SUCCESS, "인터뷰 생성에 성공하였습니다.", interviewResponseDto));
    }

    @Operation(
        summary = "후속 질문 생성",
        description = "이전 질문과 답변을 기반으로 다음 인터뷰 질문을 생성합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "후속 질문 생성 성공",
            content = @Content(schema = @Schema(implementation = InterviewResponseDto.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/questions")
    public ResponseEntity<?> createQuestion(@RequestBody QuestionRequestDto requestDto) {

        InterviewResponseDto responseDto = interviewService.createQuestion(requestDto);

        return ResponseEntity.ok(
            ApiResponse.success(
                ApiResponseCode.INTERVIEW_QUESTION_CREATE_SUCCESS, "후속 질문 생성에 성공하였습니다.", responseDto
            )
        );
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<ApiResponse<InterviewDetailsResponse>> getInterviewDetails(
        @PathVariable Long interviewId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewDetailsResponse response = interviewService.getInterviewDetails(interviewId);

        return ResponseEntity.ok(
            ApiResponse.success(
                ApiResponseCode.INTERVIEW_DETAILS_FETCH_SUCCESS,
                "면접 질문/답변/분석 조회에 성공하였습니다.",
                response)
        );
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