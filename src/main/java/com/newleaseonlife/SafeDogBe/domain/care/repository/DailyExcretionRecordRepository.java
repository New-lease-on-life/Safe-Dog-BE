package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyExcretionRecord;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ExcretionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**3월 18일 수정*/
public interface DailyExcretionRecordRepository extends JpaRepository<DailyExcretionRecord, Long> {

  /** 특정 체크리스트의 배변 기록 조회 */
  List<DailyExcretionRecord> findByDailyChecklistId(Long dailyChecklistId);

  /** 당일 특정 종류 배변 기록 조회 (중복 등록 방지) */
  Optional<DailyExcretionRecord> findByDailyChecklistIdAndExcretionType(
      Long dailyChecklistId, ExcretionType excretionType);

  /** 반려동물 날짜 기준 배변 기록 목록 */
  List<DailyExcretionRecord> findByPetIdAndRecordDateOrderByCreatedAtDesc(
      Long petId, LocalDate recordDate);
}