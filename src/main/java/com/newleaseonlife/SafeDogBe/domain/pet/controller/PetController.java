package com.newleaseonlife.SafeDogBe.domain.pet.controller;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.service.PetService;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping
    public ResponseEntity<List<PetResponse>> getMyPets(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetController] 내 반려동물 목록 조회 userId={}", principal.getUser().getId());
        List<PetResponse> response = petService.findMyPets(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 반려동물 조회 petId={}, userId={}", petId, principal.getUser().getId());
        PetResponse response = petService.findById(petId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody PetCreateRequest request) {
        log.info("[PetController] 반려동물 등록 userId={}, name={}", principal.getUser().getId(), request.getName());
        PetResponse response = petService.create(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest request) {
        log.info("[PetController] 반려동물 수정 petId={}, userId={}", petId, principal.getUser().getId());
        PetResponse response = petService.update(petId, principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 반려동물 삭제 petId={}, userId={}", petId, principal.getUser().getId());
        petService.delete(petId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }
}
