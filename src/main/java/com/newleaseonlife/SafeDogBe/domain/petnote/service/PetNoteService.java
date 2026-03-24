package com.newleaseonlife.SafeDogBe.domain.petnote.service;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.petnote.converter.PetNoteConverter;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;
import com.newleaseonlife.SafeDogBe.domain.petnote.repository.PetNoteRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetNoteErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetNoteService {

    private final PetNoteRepository petNoteRepository;
    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetNoteConverter petNoteConverter;
    private final PetGuardianRepository petGuardianRepository;

    /** 반려동물별 노트 목록(날짜 최신순). [기획 반영: OWNER, CAREGIVER 모두 조회 가능] */
    public List<PetNoteResponse> findByPetId(Long petId, Long userId) {
        ensurePetAccess(petId, userId); // ✅ 네이밍 변경 및 권한 로직 수정 적용
        List<PetNote> notes = petNoteRepository.findAllByPet_IdOrderByNoteDateDesc(petId);
        return petNoteConverter.toResponseList(notes);
    }

    /** 반려동물·특정일 노트 목록(날짜별 조회). [기획 반영: OWNER, CAREGIVER 모두 조회 가능] */
    public List<PetNoteResponse> findByPetIdAndDate(Long petId, LocalDate noteDate, Long userId) {
        ensurePetAccess(petId, userId);
        List<PetNote> notes = petNoteRepository.findAllByPet_IdAndNoteDateOrderByIdAsc(petId, noteDate);
        return petNoteConverter.toResponseList(notes);
    }

    /** 노트 단건 조회. 해당 반려동물의 보호자(OWNER/CAREGIVER) 모두 가능 */
    public PetNoteResponse findById(Long noteId, Long userId) {
        PetNote note = getNoteOrThrow(noteId);
        ensurePetAccess(note.getPetId(), userId);
        return petNoteConverter.toResponse(note);
    }

    /** 반려노트 생성. [기획 반영: OWNER 전용] */
    @Transactional
    public PetNoteResponse create(PetNoteCreateRequest request, Long userId) {
        // 1. 관리자 권한 검증
        validateOwnerPermission(request.getPetId(), userId);

        // 2. 권한이 확보되었으므로 순수하게 Pet 엔티티만 조회 (findByIdAndUserId 충돌 제거)
        Pet pet = petRepository.findById(request.getPetId())
            .orElseThrow(() -> new BusinessException(PetErrorCode.PET_NOT_FOUND));

        User writer = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        PetNote note = PetNote.builder()
            .pet(pet)
            .noteDate(request.getNoteDate())
            .content(request.getContent())
            .writtenBy(writer)
            .linkedChecklistId(request.getLinkedChecklistId())
            .build();

        return petNoteConverter.toResponse(petNoteRepository.save(note));
    }

    /** 반려노트 수정. [기획 반영: OWNER 전용] */
    @Transactional
    public PetNoteResponse update(Long noteId, PetNoteUpdateRequest request, Long userId) {
        PetNote note = getNoteOrThrow(noteId);
        // ✅ 수정 시 관리자 권한 엄격 검증
        validateOwnerPermission(note.getPetId(), userId);
        note.update(request.getContent(), request.getNoteDate());
        return petNoteConverter.toResponse(note);
    }

    /** 반려노트 삭제. [기획 반영: OWNER 전용] */
    @Transactional
    public void delete(Long noteId, Long userId) {
        PetNote note = getNoteOrThrow(noteId);
        // 🚨 기존 ensurePetOwnership(조회권한)을 validateOwnerPermission(삭제권한)으로 격상
        validateOwnerPermission(note.getPetId(), userId);
        petNoteRepository.delete(note);
    }

    /** 반려노트(케어 템플릿) 등록, 수정, 삭제 공통 권한 검증 (OWNER 만 허용) */
    public void validateOwnerPermission(Long petId, Long userId) {
        boolean isOwner = petGuardianRepository.findByPetIdAndUserId(petId, userId)
            .map(guardian -> guardian.getRole() == PetGuardianRole.OWNER)
            .orElse(false);

        if (!isOwner) {
            throw new BusinessException(CommonErrorCode.NO_PERMISSION);
        }
    }

    // ============== 내부 Helper 메서드 ==============

    private PetNote getNoteOrThrow(Long noteId) {
        return petNoteRepository.findById(noteId)
            .orElseThrow(() -> new BusinessException(PetNoteErrorCode.PET_NOTE_NOT_FOUND));
    }

    /**
     * 해당 반려동물에 대한 일반 접근(조회) 권한이 있는지 검증. (OWNER, CAREGIVER 통합)
     * 🚨 petRepository 가 아닌 petGuardianRepository 기반의 교차 검증으로 로직 전면 수정
     */
    private void ensurePetAccess(Long petId, Long userId) {
        if (!petGuardianRepository.existsByPetIdAndUserId(petId, userId)) {
            if (!petRepository.existsById(petId)) {
                throw new BusinessException(PetErrorCode.PET_NOT_FOUND);
            }
            throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
        }
    }
}