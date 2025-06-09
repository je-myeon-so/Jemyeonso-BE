package com.jemyeonso.app.jemyeonsobe.api.document.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileService {

    private final AmazonS3 amazonS3;

    @Value("${AWS_S3_BUCKET:jemyeonso-bucket}")
    private String bucketName;

    @Value("${AWS_REGION_STATIC}")
    private String region;

    public String uploadFile(MultipartFile file, Long userId, String documentType) throws IOException {
        // 원본 파일명 사용
        String originalFileName = file.getOriginalFilename();

        // S3 키 생성: documents/{documentType}s/{원본파일명}
        String s3Key = "documents/" + documentType + "s/" + originalFileName;

        try {
            // 메타데이터 설정
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/pdf");
            metadata.setContentLength(file.getSize());

            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName, s3Key, file.getInputStream(), metadata);

            amazonS3.putObject(putObjectRequest);

            // S3 URL 생성
            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, region, s3Key);

            log.info("File uploaded successfully to S3: {}", s3Url);
            return s3Url;

        } catch (Exception e) {
            log.error("Failed to upload file to S3", e);
            throw new RuntimeException("S3 파일 업로드에 실패했습니다.", e);
        }
    }
}
