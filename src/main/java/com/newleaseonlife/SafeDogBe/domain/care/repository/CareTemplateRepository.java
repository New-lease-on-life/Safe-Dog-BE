package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/** 3월 18일 수정
 * ✅ 변경: findAllActiveTemplatesByCycleWithPet → findAllActiveTemplatesWithPet
 *   (RepeatCycle 제거로 쿼리 변경. 주기 계산은 Service에서 shouldGenerateToday()로 처리)
 */
public interface CareTemplateRepository extends JpaRepository<CareTemplate, Long> {

  /** 특정 반려동물의 활성 템플릿 목록 */
  @Query("SELECT ct FROM CareTemplate ct WHERE ct.pet.id = :petId AND ct.isActive = true ORDER BY ct.id ASC")
  List<CareTemplate> findActiveByPetId(Long petId);

  /**
   * 스케줄러용: 전체 활성 템플릿 + pet 페치조인.
   * ✅ 변경: 기존 RepeatCycle.DAILY 필터 제거 → shouldGenerateToday()로 판단
   */
  @Query("SELECT ct FROM CareTemplate ct JOIN FETCH ct.pet WHERE ct.isActive = true")
  List<CareTemplate> findAllActiveTemplatesWithPet();
}
