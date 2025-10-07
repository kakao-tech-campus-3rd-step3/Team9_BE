package com.pado.domain.quiz.service;

import com.pado.domain.material.entity.File;
import com.pado.domain.material.repository.FileRepository;
import com.pado.domain.s3.service.S3Service;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileProcessingService {

    private final S3Service s3Service;
    private final FileRepository fileRepository;

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    @Transactional
    public String processFileAndUpdateState(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        try (InputStream inputStream = s3Service.downloadFileAsStream(file.getFileKey());
             BufferedInputStream bufferedStream = new BufferedInputStream(inputStream)) {

            Tika tika = new Tika();

            // 파일 타입 확인
            bufferedStream.mark(Integer.MAX_VALUE);
            String detectedMimeType = tika.detect(bufferedStream, file.getName());
            bufferedStream.reset();

            // 허용된 타입인지 체크
            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new BusinessException(ErrorCode.INVALID_FILE_FORMAT, "Disallowed MIME type: " + detectedMimeType);
            }

            // 텍스트 추출 & 전처리
            String extractedText = tika.parseToString(bufferedStream);
            String cleanedText = extractedText.replaceAll("\\s+", " ").trim();

            // 파일 상태 업데이트
            file.markAsCompleted(cleanedText, detectedMimeType);

            return cleanedText;

        } catch (Exception e) {
            file.markAsFailed();
            log.error("Failed to process file ID {}: {}", file.getId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.S3_SERVICE_ERROR,
                    "File processing failed for fileId: " + file.getId());
        }
    }
}
