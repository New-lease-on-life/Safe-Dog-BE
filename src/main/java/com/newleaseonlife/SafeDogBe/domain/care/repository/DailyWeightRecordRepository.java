package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyWeightRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/** 3월 18일 수정 */
public interface DailyWeightRecordRepository extends JpaRepository<DailyWeightRecord, Long> {

  /** 특정 날짜 체중 기록 (당일 1건) */
  Optional<DailyWeightRecord> findByPetIdAndRecordDate(Long petId, LocalDate recordDate);

  /** 최근 N일 체중 기록 이력 */
  List<DailyWeightRecord> findByPetIdAndRecordDateBetweenOrderByRecordDateDesc(
      Long petId, LocalDate from, LocalDate to);
}