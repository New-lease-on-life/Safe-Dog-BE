package com.newleaseonlife.SafeDogBe.domain.pet.repository;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 반려동물-보호자(pet_guardian) 영속화.
 * Pet 소유자만 추가/삭제 가능하므로, 서비스에서 소유권 검사 후 사용.
 */
public interface PetGuardianRepository extends JpaRepository<PetGuardian, Long> {

    /** 해당 반려동물의 보호자 목록 조회 */
    List<PetGuardian> findByPetIdOrderByIdAsc(Long petId);

    /** 특정 반려동물·사용자 조합의 보호자 연결 조회 */
    Optional<PetGuardian> findByPetIdAndUserId(Long petId, Long userId);

    /** 특정 반려동물에 해당 사용자가 보호자로 등록되어 있는지 여부 */
    boolean existsByPetIdAndUserId(Long petId, Long userId);

    /** 특정 회원이 주어진 역할(OWNER 등)로 등록된 반려동물이 있는지 여부. 탈퇴 방어 등에서 사용 */
    boolean existsByUser_IdAndRole(Long userId, PetGuardianRole role);
}
