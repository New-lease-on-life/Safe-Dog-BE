package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.ChecklistHistoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChecklistHistoryLogRepository extends JpaRepository<ChecklistHistoryLog, Long> {

  // 1. 특정 체크리스트 항목의 변경 이력 조회 (최신순 정렬)
  // N+1 방지를 위해 액션을 수행한 유저 정보도 함께 Fetch Join
  @Query("SELECT l FROM ChecklistHistoryLog l JOIN FETCH l.user WHERE l.dailyChecklist.id = :checklistId ORDER BY l.createdAt DESC")
  List<ChecklistHistoryLog> findByDailyChecklistIdWithUser(@Param("checklistId") Long checklistId);

  // 2. 백엔드 핵심: 90일이 지난 오래된 로그 대량 삭제 (Batch 스케줄러용)
  // @Modifying을 사용하여 영속성 컨텍스트를 우회하고 DB에 직접 DELETE 쿼리를 날려 성능 최적화
  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM ChecklistHistoryLog l WHERE l.createdAt < :cutoffDate")
  int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}