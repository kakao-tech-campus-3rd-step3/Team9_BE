package com.pado.domain.reflection.controller;

import com.pado.domain.reflection.dto.*;
import com.pado.domain.reflection.service.ReflectionService;
import com.pado.global.auth.annotation.CurrentUser;
import com.pado.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReflectionController {

    private final ReflectionService reflectionService;

    @PostMapping("/studies/{study_id}/reflections")
    public ResponseEntity<ReflectionResponseDto> createReflection(
        @PathVariable("study_id") Long studyId,
        @CurrentUser User user,
        @RequestBody ReflectionCreateRequestDto request
    ) {
        return ResponseEntity.ok(reflectionService.createReflection(studyId, user, request));
    }

    @GetMapping("/studies/{study_id}/reflections")
    public ResponseEntity<List<ReflectionResponseDto>> getReflections(
        @PathVariable("study_id") Long studyId,
        @CurrentUser User user) {
        return ResponseEntity.ok(reflectionService.getReflections(studyId, user));
    }

    @GetMapping("/reflections/{reflection_id}")
    public ResponseEntity<ReflectionResponseDto> getReflection(
        @PathVariable("reflection_id") Long reflectionId,
        @CurrentUser User user) {
        return ResponseEntity.ok(reflectionService.getReflection(reflectionId, user));
    }

    @PatchMapping("/reflections/{reflection_id}")
    public ResponseEntity<ReflectionResponseDto> updateReflection(
        @PathVariable("reflection_id") Long reflectionId,
        @CurrentUser User user,
        @RequestBody ReflectionCreateRequestDto request
    ) {
        return ResponseEntity.ok(reflectionService.updateReflection(reflectionId, user, request));
    }

    @DeleteMapping("/reflections/{reflection_id}")
    public ResponseEntity<Void> deleteReflection(
        @PathVariable("reflection_id") Long reflectionId,
        @CurrentUser User user) {
        reflectionService.deleteReflection(reflectionId, user);
        return ResponseEntity.noContent().build();
    }
}