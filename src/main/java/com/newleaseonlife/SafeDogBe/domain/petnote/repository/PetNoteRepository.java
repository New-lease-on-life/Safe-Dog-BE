package com.newleaseonlife.SafeDogBe.domain.petnote.repository;

import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 반려노트(pet_notes) 영속화. Pet 연관관계 기준 조회.
 */
public interface PetNoteRepository extends JpaRepository<PetNote, Long> {

    /** 해당 반려동물의 노트 목록. 날짜 최신순 */
    List<PetNote> findAllByPet_IdOrderByNoteDateDesc(Long petId);

    /** 해당 반려동물·특정일의 노트 목록. 날짜별 조회용 */
    List<PetNote> findAllByPet_IdAndNoteDateOrderByIdAsc(Long petId, LocalDate noteDate);

    /** ID + petId로 단건 조회 */
    Optional<PetNote> findByIdAndPet_Id(Long id, Long petId);

    /** 해당 반려동물에 노트 존재 여부 */
    boolean existsByPet_Id(Long petId);
}
