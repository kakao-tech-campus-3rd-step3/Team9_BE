package com.pado.domain.s3.service;

import com.pado.domain.s3.dto.DownloadFilePresignedUrlRequestDto;
import com.pado.domain.s3.dto.DownloadFilePresignedUrlResponseDto;
import com.pado.domain.s3.dto.UploadFilePreSignedUrlRequestDto;
import com.pado.domain.s3.dto.UploadFilePreSignedUrlResponseDto;
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

    // 업로드용
    public UploadFilePreSignedUrlResponseDto createUploadPresignedUrl(UploadFilePreSignedUrlRequestDto request) {
        String fileName = request.name();
        String key = generateFileKey(fileName);
        String presignedUrl = generatePresignedUploadUrl(key);

        return new UploadFilePreSignedUrlResponseDto(presignedUrl, key);
    }

    // 다운로드용
    public DownloadFilePresignedUrlResponseDto createDownloadPresignedUrl(DownloadFilePresignedUrlRequestDto request) {
        String fileName = request.fileName();
        String fileKey = request.fileKey();

        // 파일 존재 여부 확인
        if (!doesFileExist(fileKey)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        String presignedUrl = generatePresignedDownloadUrl(fileName, fileKey);

        return new DownloadFilePresignedUrlResponseDto(presignedUrl);
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
            // String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            // String contentDisposition = "attachment; filename=\"" + encodedFileName + "\"";
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
