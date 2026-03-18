package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.converter.ExcretionRecordConverter;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.ExcretionRecordRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.ExcretionRecordResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyExcretionRecord;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyExcretionRecordRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CareErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 3월 18일 수정 배변 기록 서비스. 기획서 3: 소변/대변 상태(정상/이상) 및 세부 항목 저장.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExcretionRecordService {

  private final DailyExcretionRecordRepository excretionRecordRepository;
  private final DailyChecklistRepository checklistRepository;
  private final UserRepository userRepository;
  private final ExcretionRecordConverter converter;

  /**
   * 배변 기록 등록 또는 수정 (당일만 가능)
   */
  @Transactional
  public ExcretionRecordResponse saveExcretionRecord(Long userId, ExcretionRecordRequest req) {
    DailyChecklist checklist = checklistRepository.findById(req.getDailyChecklistId())
        .orElseThrow(() -> new BusinessException(CareErrorCode.CHECKLIST_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    validateTodayOnly(checklist);

    // ✅ 수정: req.getIsNormal()이 null인 경우 NPE 방지
    if (req.getIsNormal() == null) {
      throw new BusinessException(CareErrorCode.CHECKLIST_NOT_FOUND); // 또는 별도 유효성 에러코드
    }

    DailyExcretionRecord record = excretionRecordRepository
        .findByDailyChecklistIdAndExcretionType(req.getDailyChecklistId(), req.getExcretionType())
        .orElseGet(() -> DailyExcretionRecord.builder()
            .dailyChecklist(checklist)
            .pet(checklist.getPet())   // ✅ checklist에서 자동 참조
            .recordDate(checklist.getTargetDate())
            .excretionType(req.getExcretionType())
            .isNormal(req.getIsNormal())
            .build());

    record.update(
        req.getIsNormal(),  // Boolean → Boolean (DailyExcretionRecord.update 파라미터도 Boolean으로 통일)
        req.getUrineCount(), req.getUrineColor(), req.getIsUrineAccident(),
        req.getFecesCount(), req.getFecesCondition(),
        user
    );
    excretionRecordRepository.save(record);
    return converter.toResponse(record);
  }

  /**
   * 특정 체크리스트의 배변 기록 목록
   */
  public List<ExcretionRecordResponse> getByChecklist(Long checklistId) {
    return converter.toResponseList(
        excretionRecordRepository.findByDailyChecklistId(checklistId));
  }

  private void validateTodayOnly(DailyChecklist checklist) {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    if (!checklist.getTargetDate().equals(today)) {
      throw new BusinessException(CareErrorCode.CHECKLIST_DATE_NOT_TODAY);
    }
  }
}