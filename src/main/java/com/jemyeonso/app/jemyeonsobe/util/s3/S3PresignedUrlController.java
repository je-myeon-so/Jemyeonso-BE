package com.jemyeonso.app.jemyeonsobe.util.s3;

import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backend/upload")
@Tag(name = "S3 Upload", description = "S3 Presigned URL 관련 API입니다.")
public class S3PresignedUrlController {

    private final S3PresignedUrlService s3PresignedUrlService;

    @GetMapping("/presigned-url")
    @Operation(summary = "S3 Presigned URL 조회", description = "주어진 파일 이름을 기반으로 S3 Presigned URL, 객체 URL을 생성합니다.")
    public ResponseEntity<?> uploadPresignedUrl(
        @Parameter(description = "업로드할 파일 이름 (확장자 포함)", example = "profile.png")
        @RequestParam String fileName) {
        String uuidFileName = UUID.randomUUID() + "_" + fileName;

        S3UrlResponseDto responseDto = s3PresignedUrlService.createS3Url(uuidFileName);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "S3 PresignedUrl 조회 성공", responseDto));
    }
}
