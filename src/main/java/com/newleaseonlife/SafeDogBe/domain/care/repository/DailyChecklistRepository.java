package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyChecklistRepository extends JpaRepository<DailyChecklist, Long> {

  // 1. 메인 화면: 특정 반려동물의 '오늘(특정 날짜)' 할 일 목록 조회 (N+1 방지 페치 조인)
  @Query("SELECT d FROM DailyChecklist d LEFT JOIN FETCH d.completedBy WHERE d.pet.id = :petId AND d.targetDate = :targetDate")
  List<DailyChecklist> findAllByPetIdAndTargetDateWithUser(@Param("petId") Long petId, @Param("targetDate") LocalDate targetDate);

  // 2. 스케줄러 방어 로직: 이미 오늘 날짜로 해당 템플릿 기반의 체크리스트가 생성되었는지 확인
  boolean existsByCareTemplateIdAndTargetDate(Long careTemplateId, LocalDate targetDate);
}