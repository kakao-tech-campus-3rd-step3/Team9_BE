package com.pado.domain.s3.service;

import com.pado.domain.s3.dto.PreSignedUrlRequestDto;
import com.pado.domain.s3.dto.PreSignedUrlResponseDto;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public PreSignedUrlResponseDto createPresignedUrl(PreSignedUrlRequestDto request) {
        String fileName = request.name();

        String presignedUrl = generatePresignedUploadUrl(fileName);
        String fileUrl = getFileUrl(fileName);

        return new PreSignedUrlResponseDto(presignedUrl, fileUrl);
    }

    //파일 업로드용 Presigned URL 생성 (15분 유효)
    public String generatePresignedUploadUrl(String fileName) {
        try {
            String key = generateFileKey(fileName);
            
            PutObjectPresignRequest putObjectRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .putObjectRequest(req -> req.bucket(bucketName).key(key))
                    .build();
            
            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(putObjectRequest);
            String presignedUrl = presignedRequest.url().toString();
            
            return presignedUrl;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }


    // 업로드된 파일의 공개 접근 URL 생성
    public String getFileUrl(String fileName) {
        try {
            String key = generateFileKey(fileName);
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, getRegion(), key);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }


    // S3에서 파일 삭제 (URL 기준)
    public void deleteFileByUrl(String fileUrl) {
        Optional<String> keyOpt = extractS3KeyFromUrl(fileUrl);
        String key = keyOpt.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_FILE_FORMAT));
        
        try {
            // 파일 존재 확인
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.headObject(headRequest);
            
            // 파일 삭제
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            
            s3Client.deleteObject(deleteRequest);
        } catch (NoSuchKeyException e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // 파일명으로 S3 키 생성 (UUID + 확장자)
    private String generateFileKey(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT);
        }
        
        String uuid = UUID.randomUUID().toString();
        String extension = "";
        
        if (fileName.contains(".")) {
            extension = fileName.substring(fileName.lastIndexOf("."));
        }
        
        return uuid + extension;
    }

    // URL에서 키 추출
    private Optional<String> extractS3KeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return Optional.empty();
        }
        
        try {
            if (url.contains("amazonaws.com/")) {
                return Optional.of(url.substring(url.indexOf("amazonaws.com/") + 14));
            }
            // CloudFront URL 처리
            if (url.contains("cloudfront.net/")) {
                return Optional.of(url.substring(url.indexOf("cloudfront.net/") + 15));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // 현재 설정된 리전 반환
    private String getRegion() {
        try {
            return s3Client.serviceClientConfiguration().region().id();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }
}
