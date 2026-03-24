package com.newleaseonlife.SafeDogBe.domain.petnote.repository;

import com.newleaseonlife.SafeDogBe.domain.petnote.entity.ChecklistMemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistMemoRepository extends JpaRepository<ChecklistMemo, Long> {

  // N+1 문제 방지를 위해 작성자(author) 패치 조인 적용
  @Query("SELECT m FROM ChecklistMemo m JOIN FETCH m.author WHERE m.dailyChecklist.id = :checklistId ORDER BY m.createdAt ASC")
  List<ChecklistMemo> findAllByDailyChecklistIdWithAuthor(@Param("checklistId") Long checklistId);
}