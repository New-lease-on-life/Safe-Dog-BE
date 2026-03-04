package com.newleaseonlife.SafeDogBe.domain.petnote.service;

import com.newleaseonlife.SafeDogBe.domain.petnote.converter.PetNoteConverter;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;
import com.newleaseonlife.SafeDogBe.domain.petnote.repository.PetNoteRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetNoteErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 추후 연결: Pet 도메인 머지 후 아래를 적용할 예정.
 * - create 시: PetRepository.findById(request.getPetId()) 또는 PetService.findById(petId, userId) 로
 *   해당 반려동물 존재 여부 및 소유자(userId) 검증 후 저장.
 * - findById, findByPetId, update, delete 시: 해당 노트의 petId로 Pet 조회 후
 *   Pet의 소유자(userId)가 현재 로그인 사용자와 일치하는지 검증 (PetService 또는 PetRepository.findByUserId 활용).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetNoteService {

    private final PetNoteRepository petNoteRepository;
    private final PetNoteConverter petNoteConverter;

    public List<PetNoteResponse> findByPetId(Long petId, Long userId) {
        log.debug("[PetNoteService] findByPetId petId={}, userId={}", petId, userId);
        // 추후 연결: Pet 도메인 머지 후 해당 petId가 userId 소유인지 검증 (PetService.findById(petId, userId) 등)
        List<PetNote> notes = petNoteRepository.findAllByPetIdOrderByNoteDateDesc(petId);
        return petNoteConverter.toResponseList(notes);
    }

    public List<PetNoteResponse> findByPetIdAndDate(Long petId, LocalDate noteDate, Long userId) {
        log.debug("[PetNoteService] findByPetIdAndDate petId={}, noteDate={}, userId={}", petId, noteDate, userId);
        // 추후 연결: Pet 도메인 머지 후 해당 petId가 userId 소유인지 검증
        List<PetNote> notes = petNoteRepository.findAllByPetIdAndNoteDateOrderByIdAsc(petId, noteDate);
        return petNoteConverter.toResponseList(notes);
    }

    public PetNoteResponse findById(Long noteId, Long userId) {
        log.debug("[PetNoteService] findById noteId={}, userId={}", noteId, userId);
        PetNote note = getNoteOrThrow(noteId);
        // 추후 연결: Pet 도메인 머지 후 note.getPetId()에 해당하는 Pet이 userId 소유인지 검증
        return petNoteConverter.toResponse(note);
    }

    @Transactional
    public PetNoteResponse create(PetNoteCreateRequest request, Long userId) {
        log.info("[PetNoteService] create petId={}, noteDate={}, userId={}",
                request.getPetId(), request.getNoteDate(), userId);
        // 추후 연결: Pet 도메인 머지 후 PetRepository.findById(request.getPetId()) 또는
        // PetService.findById(request.getPetId(), userId) 로 존재 및 소유권 검증
        PetNote note = PetNote.builder()
                .petId(request.getPetId())
                .noteDate(request.getNoteDate())
                .content(request.getContent())
                .build();
        PetNote saved = petNoteRepository.save(note);
        log.info("[PetNoteService] create 완료 noteId={}", saved.getId());
        return petNoteConverter.toResponse(saved);
    }

    @Transactional
    public PetNoteResponse update(Long noteId, PetNoteUpdateRequest request, Long userId) {
        log.info("[PetNoteService] update noteId={}, userId={}", noteId, userId);
        PetNote note = getNoteOrThrow(noteId);
        // 추후 연결: Pet 도메인 머지 후 note.getPetId()에 해당하는 Pet이 userId 소유인지 검증 후 수정
        note.update(request.getContent());
        return petNoteConverter.toResponse(note);
    }

    @Transactional
    public void delete(Long noteId, Long userId) {
        log.info("[PetNoteService] delete noteId={}, userId={}", noteId, userId);
        PetNote note = getNoteOrThrow(noteId);
        // 추후 연결: Pet 도메인 머지 후 note.getPetId()에 해당하는 Pet이 userId 소유인지 검증 후 삭제
        petNoteRepository.delete(note);
        log.info("[PetNoteService] delete 완료 noteId={}", noteId);
    }

    private PetNote getNoteOrThrow(Long noteId) {
        return petNoteRepository.findById(noteId)
                .orElseThrow(() -> {
                    log.warn("[PetNoteService] 반려노트 없음 noteId={}", noteId);
                    return new BusinessException(PetNoteErrorCode.PET_NOTE_NOT_FOUND);
                });
    }
}
