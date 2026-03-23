package com.newleaseonlife.SafeDogBe.domain.pet.controller;

import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.FormValidationCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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
import org.springframework.validation.annotation.Validated;
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
@Tag(name = "Pet 도메인(개발완)", description = "반려동물 등록·조회·수정·삭제, 공동 보호자 관리 API")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(summary = "내 반려동물 목록 조회", description = "소유하거나 공유받은 Pet 목록을 반환합니다(최신순).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<PetResponse>> getMyPets(
            @AuthenticationPrincipal CustomPrincipal principal) {
        log.info("[PetController] 내 반려동물 목록 조회 userId={}", principal.getUser().getId());
        List<PetResponse> response = petService.findMyPets(principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "반려동물 단건 조회", description = "권한(OWNER)이 있는 반려동물의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        }))
    })
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 반려동물 조회 petId={}, userId={}", petId, principal.getUser().getId());
        PetResponse response = petService.findById(petId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    //--------------------------반려동물 등록---------------------

    @Operation(summary = "반려동물 등록", description = "신규 반려동물을 등록합니다. 등록 시 요청 유저가 OWNER로 자동 설정됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "필수값 누락", value = "{\"code\": 400, \"message\": \"잘못된 요청 데이터입니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "사용자 조회 실패", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "유저 없음", value = "{\"code\": 404, \"message\": \"사용자를 찾을 수 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "409", description = "데이터 중복", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "이름 중복", value = "{\"code\": 409, \"message\": \"이미 동일한 이름의 반려동물이 등록되어 있습니다.\"}"),
            @ExampleObject(name = "등록번호 중복", value = "{\"code\": 409, \"message\": \"이미 등록된 반려동물 등록번호입니다.\"}")
        }))
    })
    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @Valid @RequestBody PetCreateRequest request) {
        log.info("[PetController] 반려동물 등록 userId={}, name={}", principal.getUser().getId(), request.getName());
        PetResponse response = petService.create(principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //--------------------------반려동물 수정--------------------
    @Operation(summary = "반려동물 정보 수정", description = "반려동물 정보를 수정합니다. 소유자(OWNER) 권한이 필요하며, 수정 시 이름/등록번호 중복 검사를 재수행합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "입력값 검증 실패", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "이름 누락", value = "{\"code\": 400, \"message\": \"반려동물 이름은 필수 입력값입니다.\"}"),
            @ExampleObject(name = "생일 누락", value = "{\"code\": 400, \"message\": \"반려동물 생일은 필수 입력값입니다.\"}")
        })),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "409", description = "데이터 중복", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "이름 중복", value = "{\"code\": 409, \"message\": \"이미 동일한 이름의 반려동물이 등록되어 있습니다.\"}"),
            @ExampleObject(name = "등록번호 중복", value = "{\"code\": 409, \"message\": \"이미 등록된 반려동물 등록번호입니다.\"}")
        }))
    })
    @PatchMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest request) {

        validatePetUpdateRequest(request);

        PetResponse response = petService.update(petId, principal.getUser().getId(), request);
        return ResponseEntity.ok(response);
    }

    private void validatePetUpdateRequest(PetUpdateRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BusinessException(FormValidationCode.PET_NAME_REQUIRED);
        }

        if (request.getBirthDate() == null) {
            throw new BusinessException(FormValidationCode.PET_BIRTH_REQUIRED);
        }
    }

    @Operation(summary = "반려동물 삭제", description = "소유자만 삭제 가능합니다. 관련된 보호자 연결 정보도 함께 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        }))
    })
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 반려동물 삭제 petId={}, userId={}", petId, principal.getUser().getId());
        petService.delete(petId, principal.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "보호자 목록 조회", description = "해당 반려동물에 등록된 보호자 목록을 조회합니다. 소유자 권한이 필요합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        }))
    })
    @GetMapping("/{petId}/guardians")
    public ResponseEntity<List<PetGuardianResponse>> getGuardians(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId) {
        log.info("[PetController] 보호자 목록 조회 petId={}, userId={}", petId, principal.getUser().getId());
        List<PetGuardianResponse> response = petService.getGuardians(petId, principal.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "보호자 직접 추가", description = "소유자가 특정 유저를 보호자로 직접 추가합니다. 이미 등록된 유저인 경우 409 에러가 발생합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "추가 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "대상 유저 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "유저 조회 실패", value = "{\"code\": 404, \"message\": \"사용자를 찾을 수 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "409", description = "보호자 중복", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "이미 등록된 보호자", value = "{\"code\": 409, \"message\": \"이미 해당 반려동물의 보호자로 등록된 사용자입니다.\"}")
        }))
    })
    @PostMapping("/{petId}/guardians")
    public ResponseEntity<PetGuardianResponse> addGuardian(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @Valid @RequestBody GuardianAddRequest request) {
        log.info("[PetController] 보호자 추가 petId={}, userId={}", petId, principal.getUser().getId());
        PetGuardianResponse response = petService.addGuardian(petId, principal.getUser().getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "보호자 제거", description = "소유자가 특정 보호자를 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "제거 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "삭제 대상 보호자 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "보호자 찾을 수 없음", value = "{\"code\": 404, \"message\": \"등록된 보호자 정보를 찾을 수 없습니다.\"}")
        }))
    })
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

    @Operation(summary = "관리자(OWNER) 변경", description = "현재 OWNER가 다른 보호자에게 OWNER 권한을 넘기고 CAREGIVER로 변경됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "권한 변경 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "변경 권한 없음", value = "{\"code\": 403, \"message\": \"해당 권한이 없습니다.\"}"), // CommonErrorCode.NO_PERMISSION
            @ExampleObject(name = "접근 권한 부족", value = "{\"code\": 403, \"message\": \"해당 반려동물에 접근 권한이 없습니다.\"}")
        })),
        @ApiResponse(responseCode = "404", description = "보호자 정보 없음", content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "새 소유자/기존 소유자 찾을 수 없음", value = "{\"code\": 404, \"message\": \"등록된 보호자 정보를 찾을 수 없습니다.\"}")
        }))
    })
    @PostMapping("/{petId}/guardians/{newOwnerUserId}/make-owner")
    public ResponseEntity<List<PetGuardianResponse>> makeOwner(
            @AuthenticationPrincipal CustomPrincipal principal,
            @PathVariable Long petId,
            @PathVariable Long newOwnerUserId) {
        Long currentUserId = principal.getUser().getId();
        log.info("[PetController] makeOwner petId={}, currentOwnerUserId={}, newOwnerUserId={}",
                petId, currentUserId, newOwnerUserId);

        List<PetGuardianResponse> response =
                petService.changePetOwner(petId, currentUserId, newOwnerUserId);
        return ResponseEntity.ok(response);
    }
}
