package com.jemyeonso.app.jemyeonsobe.util.s3;

import com.jemyeonso.app.jemyeonsobe.common.enums.ApiResponseCode;
import com.jemyeonso.app.jemyeonsobe.common.exception.ApiResponse;
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
public class S3PresignedUrlController {

    private final S3PresignedUrlService s3PresignedUrlService;

    @GetMapping("/presigned-url")
    public ResponseEntity<?> uploadPresignedUrl(@RequestParam String fileName) {
        String uuidFileName = UUID.randomUUID() + "_" + fileName;

        S3UrlResponseDto responseDto = s3PresignedUrlService.createS3Url(uuidFileName);

        return ResponseEntity.ok(ApiResponse.success(ApiResponseCode.SUCCESS, "S3 PresignedUrl 조회 성공", responseDto));
    }
}
