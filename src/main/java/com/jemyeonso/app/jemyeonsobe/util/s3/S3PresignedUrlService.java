package com.jemyeonso.app.jemyeonsobe.util.s3;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.net.URL;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final AmazonS3 amazonS3;

    @Value("${S3_BUCKET_NAME}")
    private String bucket;

    public S3UrlResponseDto createS3Url(String uuidFileName) {
        String fileUrl = null;
        String presignedUrl = null;

        // fileUrl, presignedUrl 생성
        fileUrl = getFileUrl(uuidFileName);
        presignedUrl = generatePresignedUrl(uuidFileName);

        S3UrlResponseDto responseDto = S3UrlResponseDto.builder()
            .fileUrl(fileUrl)
            .presignedUrl(presignedUrl)
            .build();

        return responseDto;
    }

    public String generatePresignedUrl(String uuidFileName) {
        log.info("Service: Generate presigned url for file {}", uuidFileName);
        String key = "profile/" + uuidFileName;                             // S3의 profile 폴더에 저장

        Date expiration = new Date();
        expiration.setTime(expiration.getTime() + 1000 * 60 * 3);           // presignedURl 유효시간: 3분

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.PUT)
                .withExpiration(expiration);

        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    // fileUrl 생성
    public String getFileUrl(String fileName) {
        String key = "profile/" + fileName;
        return amazonS3.getUrl(bucket, key).toString();
    }
}