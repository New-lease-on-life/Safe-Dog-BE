package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.converter.DailyChecklistConverter;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.ChecklistHistoryLog;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ChecklistActionType;
import com.newleaseonlife.SafeDogBe.domain.care.repository.ChecklistHistoryLogRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyChecklistService {

  private final DailyChecklistRepository dailyChecklistRepository;
  private final ChecklistHistoryLogRepository historyLogRepository;
  private final UserRepository userRepository;
  private final DailyChecklistConverter checklistConverter;

  @Transactional
  public DailyChecklistResponse completeChecklist(Long checklistId, Long userId) {
    // 1. 체크리스트와 유저 조회
    DailyChecklist checklist = dailyChecklistRepository.findById(checklistId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 할 일입니다."));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

    // 2. 이미 완료된 항목인지 1차 방어 로직 (JPA 낙관적 락이 2차 방어 수행)
    if (checklist.isCompleted()) {
      throw new IllegalStateException("이미 완료 처리된 항목입니다.");
    }

    // 3. 도메인 로직 호출: 완료 상태 및 행위자 업데이트 (더티 체킹 발생)
    checklist.complete(user);

    // 4. 부수 효과(Side Effect): 히스토리 로그 생성 및 저장
    ChecklistHistoryLog log = ChecklistHistoryLog.builder()
        .dailyChecklist(checklist)
        .user(user)
        .actionType(ChecklistActionType.CHECK)
        .build();
    historyLogRepository.save(log);

    // 5. 프론트엔드로 최신 상태(Version 포함) 반환
    return checklistConverter.toResponse(checklist);
  }

  @Transactional
  public DailyChecklistResponse uncompleteChecklist(Long checklistId, Long userId) {
    DailyChecklist checklist = dailyChecklistRepository.findById(checklistId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 할 일입니다."));
    User user = userRepository.findById(userId) // 취소를 누른 사람의 정보
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

    if (!checklist.isCompleted()) {
      throw new IllegalStateException("아직 완료되지 않은 항목입니다.");
    }

    // 도메인 로직: 상태 초기화
    checklist.uncomplete();

    // 부수 효과: "취소" 로그 기록 (누가 취소했는지 추적)
    ChecklistHistoryLog log = ChecklistHistoryLog.builder()
        .dailyChecklist(checklist)
        .user(user)
        .actionType(ChecklistActionType.UNCHECK)
        .build();
    historyLogRepository.save(log);

    return checklistConverter.toResponse(checklist);
  }
}