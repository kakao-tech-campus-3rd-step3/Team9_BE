package com.pado.domain.reflection.controller;

import com.pado.domain.reflection.dto.*;
import com.pado.domain.reflection.service.ReflectionService;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
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
    private final StudyMemberRepository studyMemberRepository;

    @PostMapping("/studies/{study_id}/reflections")
    public ResponseEntity<ReflectionResponseDto> createReflection(
        @PathVariable("study_id") Long studyId,
        @CurrentUser User user,
        @RequestBody ReflectionCreateRequestDto request
    ) {
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId,
                user.getId())
            .orElseThrow(() -> new IllegalArgumentException("스터디 멤버가 아닙니다."));
        return ResponseEntity.ok(
            reflectionService.createReflection(studyId, studyMember.getId(), request));
    }

    @GetMapping("/studies/{study_id}/reflections")
    public ResponseEntity<List<ReflectionResponseDto>> getReflections(
        @PathVariable("study_id") Long studyId) {
        return ResponseEntity.ok(reflectionService.getReflections(studyId));
    }

    @GetMapping("/reflections/{reflection_id}")
    public ResponseEntity<ReflectionResponseDto> getReflection(
        @PathVariable("reflection_id") Long reflectionId) {
        return ResponseEntity.ok(reflectionService.getReflection(reflectionId));
    }

    @PatchMapping("/reflections/{reflection_id}")
    public ResponseEntity<ReflectionResponseDto> updateReflection(
        @PathVariable("reflection_id") Long reflectionId,
        @RequestBody ReflectionCreateRequestDto request
    ) {
        return ResponseEntity.ok(reflectionService.updateReflection(reflectionId, request));
    }

    @DeleteMapping("/reflections/{reflection_id}")
    public ResponseEntity<Void> deleteReflection(@PathVariable("reflection_id") Long reflectionId) {
        reflectionService.deleteReflection(reflectionId);
        return ResponseEntity.noContent().build();
    }
}
