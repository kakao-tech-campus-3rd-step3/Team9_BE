package com.pado.infrastruture.listener;

import com.pado.domain.material.event.MaterialDeletedEvent;
import com.pado.infrastruture.s3.S3FileDeleter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MaterialEventListener {
    private final S3FileDeleter s3FileDeleter;

    @Async
    @TransactionalEventListener
    public void handleMaterialDeletedEvent(MaterialDeletedEvent event) {
        s3FileDeleter.deleteFiles(event.fileKeys());
    }
}
