package com.newleaseonlife.SafeDogBe.domain.petnote.controller;

import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.service.PetNoteService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 반려노트(PetNote) API. CRUD 및 날짜별 조회.
 * 소유권 검증은 서비스 계층에서 수행(해당 Pet의 소유자만 조회·생성·수정·삭제 가능).
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/pet-notes")
@RequiredArgsConstructor
public class PetNoteController {

    private final PetNoteService petNoteService;

    /** 특정 반려동물의 노트 목록 조회 (날짜 최신순) */
    @GetMapping
    public ResponseEntity<List<PetNoteResponse>> getNotesByPetId(
            @RequestParam Long petId,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetNoteController] getNotesByPetId petId={}, userId={}", petId, principal.getUser().getId());
        List<PetNoteResponse> response = petNoteService.findByPetId(petId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 특정 반려동물의 특정 날짜 노트 조회 (날짜별 조회) */
    @GetMapping("/by-date")
    public ResponseEntity<List<PetNoteResponse>> getNotesByPetIdAndDate(
            @RequestParam Long petId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate noteDate,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetNoteController] getNotesByPetIdAndDate petId={}, noteDate={}, userId={}",
                petId, noteDate, principal.getUser().getId());
        List<PetNoteResponse> response = petNoteService.findByPetIdAndDate(
                petId, noteDate, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 반려노트 단건 조회 */
    @GetMapping("/{noteId}")
    public ResponseEntity<PetNoteResponse> getNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetNoteController] getNote noteId={}, userId={}", noteId, principal.getUser().getId());
        PetNoteResponse response = petNoteService.findById(noteId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 반려노트 생성 */
    @PostMapping
    public ResponseEntity<PetNoteResponse> createNote(
            @Valid @RequestBody PetNoteCreateRequest request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetNoteController] createNote petId={}, userId={}", request.getPetId(), principal.getUser().getId());
        PetNoteResponse response = petNoteService.create(request, principal.getUser().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 반려노트 수정 */
    @PatchMapping("/{noteId}")
    public ResponseEntity<PetNoteResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody PetNoteUpdateRequest request,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetNoteController] updateNote noteId={}, userId={}", noteId, principal.getUser().getId());
        PetNoteResponse response = petNoteService.update(noteId, request, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 반려노트 삭제 */
    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @PathVariable Long noteId,
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetNoteController] deleteNote noteId={}, userId={}", noteId, principal.getUser().getId());
        petNoteService.delete(noteId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
