package com.newleaseonlife.SafeDogBe.domain.pet.repository;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 반려동물(Pet) 영속화. 메인 보호자(user_id) 기준 조회·존재 여부 확인.
 */
public interface PetRepository extends JpaRepository<Pet, Long> {

    /** 메인 보호자 기준 내 반려동물 목록(최신순) */
    List<Pet> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    /** 메인 보호자 기준 내 반려동물 목록(오래된 순) */
    List<Pet> findAllByUserIdOrderByCreatedAtAsc(Long userId);

    /** ID + 메인 보호자로 단건 조회. 소유자만 조회 시 사용 */
    Optional<Pet> findByIdAndUserId(Long id, Long userId);

    /** 해당 반려동물이 해당 사용자 소유인지 여부 */
    boolean existsByIdAndUserId(Long id, Long userId);
}
