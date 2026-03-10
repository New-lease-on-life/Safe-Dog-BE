package com.newleaseonlife.SafeDogBe.domain.petnote.service;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.petnote.converter.PetNoteConverter;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;
import com.newleaseonlife.SafeDogBe.domain.petnote.repository.PetNoteRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetNoteErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 반려노트(PetNote) 도메인 서비스. CRUD 및 날짜별 조회.
 * create/조회/수정/삭제 시 해당 Pet의 소유자(userId) 검증 후 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetNoteService {

    private final PetNoteRepository petNoteRepository;
    private final PetRepository petRepository;
    private final PetNoteConverter petNoteConverter;

    /** 반려동물별 노트 목록(날짜 최신순). 소유자만 조회 가능 */
    public List<PetNoteResponse> findByPetId(Long petId, Long userId) {
        log.debug("[PetNoteService] findByPetId petId={}, userId={}", petId, userId);
        ensurePetOwnership(petId, userId);
        List<PetNote> notes = petNoteRepository.findAllByPet_IdOrderByNoteDateDesc(petId);
        return petNoteConverter.toResponseList(notes);
    }

    /** 반려동물·특정일 노트 목록(날짜별 조회). 소유자만 조회 가능 */
    public List<PetNoteResponse> findByPetIdAndDate(Long petId, LocalDate noteDate, Long userId) {
        log.debug("[PetNoteService] findByPetIdAndDate petId={}, noteDate={}, userId={}", petId, noteDate, userId);
        ensurePetOwnership(petId, userId);
        List<PetNote> notes = petNoteRepository.findAllByPet_IdAndNoteDateOrderByIdAsc(petId, noteDate);
        return petNoteConverter.toResponseList(notes);
    }

    /** 노트 단건 조회. 해당 노트의 Pet 소유자만 가능 */
    public PetNoteResponse findById(Long noteId, Long userId) {
        log.debug("[PetNoteService] findById noteId={}, userId={}", noteId, userId);
        PetNote note = getNoteOrThrow(noteId);
        ensurePetOwnership(note.getPetId(), userId);
        return petNoteConverter.toResponse(note);
    }

    /** 반려노트 생성. request.petId에 해당하는 Pet 소유자만 가능 */
    @Transactional
    public PetNoteResponse create(PetNoteCreateRequest request, Long userId) {
        log.info("[PetNoteService] create petId={}, noteDate={}, userId={}",
                request.getPetId(), request.getNoteDate(), userId);
        Pet pet = petRepository.findByIdAndUserId(request.getPetId(), userId)
                .orElseThrow(() -> {
                    if (petRepository.findById(request.getPetId()).isEmpty()) {
                        return new BusinessException(PetErrorCode.PET_NOT_FOUND);
                    }
                    return new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
                });
        PetNote note = PetNote.builder()
                .pet(pet)
                .noteDate(request.getNoteDate())
                .content(request.getContent())
                .build();
        PetNote saved = petNoteRepository.save(note);
        log.info("[PetNoteService] create 완료 noteId={}", saved.getId());
        return petNoteConverter.toResponse(saved);
    }

    /** 반려노트 수정. content·noteDate 중 전달된 값만 반영. 소유자만 가능 */
    @Transactional
    public PetNoteResponse update(Long noteId, PetNoteUpdateRequest request, Long userId) {
        log.info("[PetNoteService] update noteId={}, userId={}", noteId, userId);
        PetNote note = getNoteOrThrow(noteId);
        ensurePetOwnership(note.getPetId(), userId);
        note.update(request.getContent(), request.getNoteDate());
        return petNoteConverter.toResponse(note);
    }

    /** 반려노트 삭제. 해당 노트의 Pet 소유자만 가능 */
    @Transactional
    public void delete(Long noteId, Long userId) {
        log.info("[PetNoteService] delete noteId={}, userId={}", noteId, userId);
        PetNote note = getNoteOrThrow(noteId);
        ensurePetOwnership(note.getPetId(), userId);
        petNoteRepository.delete(note);
        log.info("[PetNoteService] delete 완료 noteId={}", noteId);
    }

    /** 노트 조회. 없으면 PET_NOTE_NOT_FOUND */
    private PetNote getNoteOrThrow(Long noteId) {
        return petNoteRepository.findById(noteId)
                .orElseThrow(() -> {
                    log.warn("[PetNoteService] 반려노트 없음 noteId={}", noteId);
                    return new BusinessException(PetNoteErrorCode.PET_NOTE_NOT_FOUND);
                });
    }

    /**
     * 해당 반려동물이 현재 사용자 소유인지 검증.
     * PetNote.pet은 nullable=false이므로 petId는 항상 non-null.
     * 소유자가 아니면 PET_NOT_FOUND 또는 PET_ACCESS_DENIED.
     */
    private void ensurePetOwnership(Long petId, Long userId) {
        if (!petRepository.existsByIdAndUserId(petId, userId)) {
            if (petRepository.findById(petId).isEmpty()) {
                throw new BusinessException(PetErrorCode.PET_NOT_FOUND);
            }
            throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
        }
    }
}
