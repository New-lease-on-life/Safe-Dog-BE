package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.converter.DailyChecklistConverter;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.DailyChecklistUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.DailyChecklistResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.ChecklistHistoryLog;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.ChecklistActionType;
import com.newleaseonlife.SafeDogBe.domain.care.repository.ChecklistHistoryLogRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CareErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/** 3월 18일 수정
 * ✅ 추가: getChecklistsByDate() — 날짜별 체크리스트 조회
 * ✅ 추가: 당일 여부 검증 (기획서 3: 오늘 날짜만 수정 가능)
 * ✅ 변경: completeChecklist/uncompleteChecklist — 날짜 검증 추가
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyChecklistService {

  private final DailyChecklistRepository dailyChecklistRepository;
  private final ChecklistHistoryLogRepository historyLogRepository;
  private final UserRepository userRepository;
  private final DailyChecklistConverter converter; // (주의) 빈 등록 되어있어야 함

  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");

  public List<DailyChecklistResponse> getChecklistsByDate(Long petId, LocalDate targetDate) {
    return converter.toResponseList(
        dailyChecklistRepository.findAllByPetIdAndTargetDateWithUser(petId, targetDate));
  }

  /**
   * 체크리스트 완료.
   * ✅ 추가: 오늘 날짜만 수정 가능 검증
   */
  @Transactional
  public DailyChecklistResponse completeChecklist(Long checklistId, Long userId) {
    DailyChecklist checklist = getChecklistOrThrow(checklistId);
    User user = getUserOrThrow(userId);

    validateTodayOnly(checklist); // ✅ 날짜 검증 통일

    if (checklist.isCompleted()) {
      throw new BusinessException(CareErrorCode.CHECKLIST_ALREADY_COMPLETED);
    }

    checklist.complete(user);
    saveLog(checklist, user, ChecklistActionType.CHECK);
    return converter.toResponse(checklist);
  }

  /**
   * 체크리스트 완료 취소.
   * ✅ 추가: 오늘 날짜만 수정 가능 검증
   */
  @Transactional
  public DailyChecklistResponse uncompleteChecklist(Long checklistId, Long userId) {
    DailyChecklist checklist = getChecklistOrThrow(checklistId);
    User user = getUserOrThrow(userId);

    validateTodayOnly(checklist); // ✅ 날짜 검증 통일

    if (!checklist.isCompleted()) {
      throw new BusinessException(CareErrorCode.CHECKLIST_NOT_COMPLETED);
    }

    checklist.uncomplete();
    saveLog(checklist, user, ChecklistActionType.UNCHECK);
    return converter.toResponse(checklist);
  }

  @Transactional
  public DailyChecklistResponse updateChecklist(Long checklistId, DailyChecklistUpdateRequest request) {
    DailyChecklist checklist = getChecklistOrThrow(checklistId);
    validateTodayOnly(checklist); // ✅ 날짜 검증 통일
    // checklist.update(request);
    return converter.toResponse(checklist);
  }


  public DailyChecklistResponse getLatestChecklist(Long checklistId) {
    log.debug("[DailyChecklistService] getLatestChecklist checklistId={}", checklistId);
    DailyChecklist checklist = getChecklistOrThrow(checklistId);
    return converter.toResponse(checklist);
  }

  public boolean hasAccessToChecklist(Long checklistId, Long userId) {
    log.debug("[DailyChecklistService] hasAccessToChecklist checklistId={}, userId={}", checklistId, userId);
    try {
      getChecklistOrThrow(checklistId);
      // 향후 PetGuardianRepository 연동을 통해 더 정교한 소유권 체크 권장
      return true;
    } catch (BusinessException e) {
      return false;
    }
  }

  /**
   * 기획서 3: "당일 체크리스트만 수정 가능. 과거 날짜는 읽기 전용"
   */
  private void validateTodayOnly(DailyChecklist checklist) {
    LocalDate todayKst = LocalDate.now(KST_ZONE);
    if (!checklist.getTargetDate().isEqual(todayKst)) {
      throw new BusinessException(CareErrorCode.CHECKLIST_DATE_NOT_TODAY);
    }
  }

  private void saveLog(DailyChecklist checklist, User user, ChecklistActionType actionType) {
    historyLogRepository.save(ChecklistHistoryLog.builder()
        .dailyChecklist(checklist)
        .user(user)
        .actionType(actionType)
        .build());
  }

  private DailyChecklist getChecklistOrThrow(Long id) {
    return dailyChecklistRepository.findById(id)
        .orElseThrow(() -> new BusinessException(CareErrorCode.CHECKLIST_NOT_FOUND));
  }

  private User getUserOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
  }
}