package com.pado.infrastruture.s3;

import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;

@Component
@RequiredArgsConstructor
public class S3FileDeleter {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    // S3에서 여러 파일 삭제
    public void deleteFiles(List<String> fileKeys) {

        if (fileKeys == null || fileKeys.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objectIdsToDelete = fileKeys.stream()
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

}