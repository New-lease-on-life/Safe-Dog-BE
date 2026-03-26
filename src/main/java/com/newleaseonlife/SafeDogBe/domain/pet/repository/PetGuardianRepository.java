package com.newleaseonlife.SafeDogBe.domain.pet.repository;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

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

    /** 특정 회원이 주어진 역할로 등록된 반려동물(보호자 연결) 목록 */
    List<PetGuardian> findByUser_IdAndRole(Long userId, PetGuardianRole role);

    /** 특정 회원이 보호자로 등록된 모든 반려동물 목록 (OWNER + CAREGIVER) */
    List<PetGuardian> findByUserId(Long userId);

    /** * ✅ 성능 최적화 버전 (Fetch Join)
     * 유저가 속한 모든 펫과, 그 펫들에 연결된 모든 보호자 정보를 한 번에 조회
     */
    @Query("SELECT pg FROM PetGuardian pg " +
        "JOIN FETCH pg.pet p " +         // 펫 정보 즉시 로딩
        "JOIN FETCH pg.user u " +        // 보호자 유저 정보 즉시 로딩
        "WHERE pg.pet.id IN (" +
        "  SELECT pg2.pet.id FROM PetGuardian pg2 WHERE pg2.user.id = :userId AND pg2.role = :role" +
        ") ORDER BY p.createdAt ASC")
    List<PetGuardian> findAllMyPetsWithGuardiansByRole(@Param("userId") Long userId, @Param("role") PetGuardianRole role);
}
