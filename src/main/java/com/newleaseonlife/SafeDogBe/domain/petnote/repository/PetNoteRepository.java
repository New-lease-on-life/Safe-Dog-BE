package com.newleaseonlife.SafeDogBe.domain.petnote.repository;

import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 추후 연결: Pet 도메인 머지 후, 소유자별 노트 조회가 필요하면
 * PetRepository.findByUserId()로 내 반려동물 목록 조회 후 petId 목록으로 조회하거나,
 * PetNote에 @ManyToOne Pet을 두고 PetNoteRepository에 findByPet_UserId(Long userId) 등 추가 가능.
 */
public interface PetNoteRepository extends JpaRepository<PetNote, Long> {

    List<PetNote> findAllByPetIdOrderByNoteDateDesc(Long petId);

    List<PetNote> findAllByPetIdAndNoteDateOrderByIdAsc(Long petId, LocalDate noteDate);

    Optional<PetNote> findByIdAndPetId(Long id, Long petId);

    boolean existsByPetId(Long petId);
}
