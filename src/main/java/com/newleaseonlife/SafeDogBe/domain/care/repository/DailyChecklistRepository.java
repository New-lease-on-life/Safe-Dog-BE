// domain/care/repository/DailyChecklistRepository.java
package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * ✅ 추가: findAllByPetIdAndTargetDateBetween (캘린더 조회용)
 */
public interface DailyChecklistRepository extends JpaRepository<DailyChecklist, Long> {

  /** 메인 화면: 특정 날짜의 반려동물 체크리스트 조회 */
  @Query("SELECT d FROM DailyChecklist d LEFT JOIN FETCH d.completedBy WHERE d.pet.id = :petId AND d.targetDate = :targetDate ORDER BY d.id ASC")
  List<DailyChecklist> findAllByPetIdAndTargetDateWithUser(@Param("petId") Long petId,
      @Param("targetDate") LocalDate targetDate);

  /** 스케줄러 중복 방지: 오늘 날짜로 해당 템플릿 기반 체크리스트 생성 여부 확인 */
  boolean existsByCareTemplateIdAndTargetDate(Long careTemplateId, LocalDate targetDate);

  /**
   * 캘린더 조회: 특정 기간 내 기록이 있는 날짜 목록 (내림차순)
   */
  @Query("SELECT DISTINCT d.targetDate FROM DailyChecklist d WHERE d.pet.id = :petId AND d.targetDate BETWEEN :from AND :to ORDER BY d.targetDate DESC")
  List<LocalDate> findDistinctDatesByPetIdAndDateBetween(@Param("petId") Long petId,
      @Param("from") LocalDate from,
      @Param("to") LocalDate to);
}