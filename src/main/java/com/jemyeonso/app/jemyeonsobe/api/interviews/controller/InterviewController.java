package com.jemyeonso.app.jemyeonsobe.api.interviews.controller;

import com.jemyeonso.app.jemyeonsobe.api.interviews.dto.*;
import com.jemyeonso.app.jemyeonsobe.api.interviews.repository.QuestionRepository;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponse;
import com.jemyeonso.app.jemyeonsobe.api.interviews.service.InterviewService;
import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    private final QuestionRepository questionRepository;

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
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewResponseDto responseDto = interviewService.createQuestion(requestDto, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        ApiResponseCode.INTERVIEW_QUESTION_CREATE_SUCCESS, "후속 질문 생성에 성공하였습니다.", responseDto
                )
        );
    }

    @GetMapping("/repository")
    @Operation(summary = "면접 목록 조회", description = "현재 로그인한 사용자의 면접 목록을 페이지네이션으로 조회합니다.")
    public ResponseEntity<ApiResponse<InterviewRepositoryResponse>> getInterviewRepository(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (1-100)") @RequestParam(defaultValue = "10") int size) {

        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ApiResponseCode.PAGINATION_INVALID_PARAMETER));
        }

        // 현재 로그인한 유저 ID 추출
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            InterviewRepositoryResponse response = interviewService.getInterviewRepository(page, size, userId);
            return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.INTERVIEW_LIST_GET_SUCCESS, response));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.error(ApiResponseCode.INTERNAL_ERROR));
        }
    }

    @GetMapping("/{interviewId}/questions")
    @Operation(
            summary = "면접 질문 목록 조회",
            description = "특정 면접의 질문 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "면접 질문 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = InterviewQuestionsResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 면접"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "접근 권한 없음")
    })
    public ResponseEntity<?> getInterviewQuestions(@PathVariable Long interviewId) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        InterviewQuestionsResponseDto responseDto = interviewService.getInterviewQuestions(interviewId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(ApiResponseCode.INTERVIEW_QUESTIONS_GET_SUCCESS, responseDto)
        );
    }
}
