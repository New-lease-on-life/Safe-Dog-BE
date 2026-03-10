package com.newleaseonlife.SafeDogBe.domain.pet.controller;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.GuardianAddRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
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

/**
 * 반려동물(Pet) API. CRUD 및 보호자(Guardian) 추가·삭제·목록 조회.
 * 소유자만 수정·삭제·보호자 관리 가능.
 */
@Slf4j
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    /** 내 반려동물 목록 조회(소유한 Pet만) */
    @GetMapping
    public ResponseEntity<List<PetResponse>> getMyPets(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetController] 내 반려동물 목록 조회 userId={}", principal.getUser().getId());
        List<PetResponse> response = petService.findMyPets(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 반려동물 단건 조회. 소유자만 가능 */
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 반려동물 조회 petId={}, userId={}", petId, principal.getUser().getId());
        PetResponse response = petService.findById(petId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 반려동물 등록. 요청자가 메인 보호자로 저장됨 */
    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody PetCreateRequest request) {
        log.info("[PetController] 반려동물 등록 userId={}, name={}", principal.getUser().getId(), request.getName());
        PetResponse response = petService.create(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 반려동물 정보 수정. 소유자만 가능 */
    @PatchMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest request) {
        log.info("[PetController] 반려동물 수정 petId={}, userId={}", petId, principal.getUser().getId());
        PetResponse response = petService.update(petId, principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    /** 반려동물 삭제. 소유자만 가능. 보호자 연결(pet_guardian) 함께 삭제 */
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 반려동물 삭제 petId={}, userId={}", petId, principal.getUser().getId());
        petService.delete(petId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    /** 해당 반려동물의 보호자 목록 조회. 소유자만 가능 */
    @GetMapping("/{petId}/guardians")
    public ResponseEntity<List<PetGuardianResponse>> getGuardians(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 보호자 목록 조회 petId={}, userId={}", petId, principal.getUser().getId());
        List<PetGuardianResponse> response = petService.getGuardians(petId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    /** 보호자 추가. 소유자만 가능. 이미 등록된 사용자면 409 */
    @PostMapping("/{petId}/guardians")
    public ResponseEntity<PetGuardianResponse> addGuardian(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @Valid @RequestBody GuardianAddRequest request) {
        log.info("[PetController] 보호자 추가 petId={}, userId={}", petId, principal.getUser().getId());
        PetGuardianResponse response = petService.addGuardian(petId, principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 보호자 제거. 소유자만 가능. guardianUserId에 해당하는 보호자 연결만 삭제 */
    @DeleteMapping("/{petId}/guardians/{guardianUserId}")
    public ResponseEntity<Void> removeGuardian(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @PathVariable Long guardianUserId) {
        log.info("[PetController] 보호자 제거 petId={}, guardianUserId={}, userId={}",
                petId, guardianUserId, principal.getUser().getId());
        petService.removeGuardian(petId, principal.getUser().getId(), guardianUserId);
        return ResponseEntity.noContent().build();
    }
}
