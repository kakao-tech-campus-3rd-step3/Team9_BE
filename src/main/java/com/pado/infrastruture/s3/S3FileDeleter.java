package com.pado.infrastruture.s3;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class S3FileDeleter {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // S3에서 여러 파일 삭제
    public void deleteFiles(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        // URL에서 키 추출
        List<ObjectIdentifier> objectIdsToDelete = fileUrls.stream()
                .map(this::extractS3KeyFromUrl)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();

        // 삭제할 파일이 없으면 작업을 종료합니다.
        if (objectIdsToDelete.isEmpty()) {
            return;
        }

        try {
            DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectIdsToDelete).quiet(true).build())
                    .build();

            // S3에 배치 삭제를 요청합니다.
            DeleteObjectsResponse response = s3Client.deleteObjects(deleteRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    // 전체 파일 URL에서 S3 객체 키 추출
    private Optional<String> extractS3KeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return Optional.empty();
        }

        try {
            if (fileUrl.contains("amazonaws.com/")) {
                return Optional.of(fileUrl.substring(fileUrl.indexOf("amazonaws.com/") + 14));
            }
            // CloudFront URL 처리
            if (fileUrl.contains("cloudfront.net/")) {
                return Optional.of(fileUrl.substring(fileUrl.indexOf("cloudfront.net/") + 15));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}