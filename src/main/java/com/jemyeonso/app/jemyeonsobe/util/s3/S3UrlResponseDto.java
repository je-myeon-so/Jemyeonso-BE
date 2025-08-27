package com.jemyeonso.app.jemyeonsobe.util.s3;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class S3UrlResponseDto {
    private String fileUrl;
    private String presignedUrl;
}
