package com.pado.domain.s3.service;

import com.pado.domain.s3.dto.*;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.s3.prefix}")
    private String s3Prefix;

    // 파일 업로드용
    public UploadPreSignedUrlResponseDto createUploadPresignedUrl(UploadFilePreSignedUrlRequestDto request) {
        String fileName = request.name();
        String key = generateFileKey(fileName);
        String presignedUrl = generatePresignedUploadUrl(key);

        return new UploadPreSignedUrlResponseDto(presignedUrl, key);
    }

    // 사진 업로드용
    public UploadPreSignedUrlResponseDto createImagePresignedUrl(UploadPhotoPresignedUrlRequestDto request) {
        String contentType = request.contentType();
        String key = generateImageKey(contentType);
        String presignedUrl = generatePresignedUploadUrl(key);

        return new UploadPreSignedUrlResponseDto(presignedUrl, key);
    }

    // 파일 다운로드용
    public DownloadPresignedUrlResponseDto createDownloadPresignedUrl(DownloadFilePresignedUrlRequestDto request) {
        String fileName = request.fileName();
        String fileKey = request.fileKey();

        // 파일 존재 여부 확인
        if (!doesFileExist(fileKey)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        String presignedUrl = generatePresignedDownloadUrl(fileName, fileKey);

        return new DownloadPresignedUrlResponseDto(presignedUrl);
    }

    // 사진 다운로드용
    public DownloadPresignedUrlResponseDto createDownloadPhotoPresignedUrl(DownloadPhotoPresignedUrlRequestDto request) {
        String fileKey = request.fileKey();

        // 파일 존재 여부 확인
        if (!doesFileExist(fileKey)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        String presignedUrl = generatePresignedDownloadUrl(fileKey);

        return new DownloadPresignedUrlResponseDto(presignedUrl);
    }

    //파일 업로드용 Presigned URL 생성 (15분 유효)
    public String generatePresignedUploadUrl(String key) {
        try {
            PutObjectPresignRequest putObjectRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .putObjectRequest(req -> req.bucket(bucketName).key(key))
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(putObjectRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }

    // 파일 다운로드용 Presigned URL 생성
    public String generatePresignedDownloadUrl(String fileName, String fileKey) {
        try {
            String contentDisposition = "attachment; filename=\"" + fileName + "\"";

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .responseContentDisposition(contentDisposition)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedGetObjectRequest.url().toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }

    // 사진 표시용 Presigned URL 생성
    public String generatePresignedDownloadUrl(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedGetObjectRequest.url().toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
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

        return s3Prefix + uuid + extension;
    }

    // 사진 다운로드 용 키 생성
    private String generateImageKey(String contentType) {
        String extension = getFileExtensionFromContentType(contentType);
        return s3Prefix + UUID.randomUUID().toString() + extension;
    }


    private String getFileExtensionFromContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT, "Content-Type이 비어있습니다.");
        }

        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            default -> throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT, "지원하지 않는 이미지 형식입니다: " + contentType);
        };
    }

    // 현재 설정된 리전 반환
    private String getRegion() {
        try {
            return s3Client.serviceClientConfiguration().region().id();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }

    // S3Service.java에 추가
    public boolean doesFileExist(String fileKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR);
        }
    }
}
