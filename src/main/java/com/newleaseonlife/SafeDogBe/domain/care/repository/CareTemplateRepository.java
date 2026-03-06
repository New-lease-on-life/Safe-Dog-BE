package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CareTemplateRepository extends JpaRepository<CareTemplate, Long> {

  // 1. 특정 반려동물의 "현재 활성화된" 케어 템플릿 목록 조회 (앱 화면 노출용)
  List<CareTemplate> findByPetIdAndIsActiveTrue(Long petId);

  // 2. 매일 자정 스케줄러(Batch) 실행 시, 전체 반려동물의 활성화된 반복 템플릿을 가져오기 위한 쿼리
  // N+1 방지를 위해 페치 조인 사용
  @Query("SELECT ct FROM CareTemplate ct JOIN FETCH ct.pet WHERE ct.isActive = true AND ct.repeatCycle = :repeatCycle")
  List<CareTemplate> findAllActiveTemplatesByCycleWithPet(@Param("repeatCycle") RepeatCycle repeatCycle);
}