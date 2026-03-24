package com.newleaseonlife.SafeDogBe.domain.careReport.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.careReport.dto.response.CareReportResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CareReportService {

  private final DailyChecklistRepository dailyChecklistRepository;
  private final PetGuardianRepository petGuardianRepository;

  private static final int MINIMUM_REQUIRED_CHECKLISTS = 7;

  public CareReportResponse getCareReport(Long userId, Long petId, int days) {

    // [예외 처리 1] 보안(인가) 검증 - IDOR 방어 (컴파일 에러 해결 완료)
    if (!petGuardianRepository.existsByPetIdAndUserId(petId, userId)) {
      throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
    }

    // [예외 처리 2] 파라미터 검증 - 서버 리소스 고갈(OOM) 방어
    if (days != 7 && days != 30) {
      throw new BusinessException(CommonErrorCode.BAD_REQUEST);
    }

    // 1. 선행조건 검증 (데이터 부족 시 UI 처리용 응답)
    long totalCount = dailyChecklistRepository.countByPet_Id(petId);
    if (totalCount < MINIMUM_REQUIRED_CHECKLISTS) {
      return CareReportResponse.builder()
          .isReportAvailable(false)
          .build();
    }

    // 2. 통계 대상 데이터 조회
    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusDays(days - 1);
    List<DailyChecklist> checklists = dailyChecklistRepository
        .findForReportByPetIdAndDateBetween(petId, startDate, endDate);

    // [예외 처리 3] 데이터 무결성 검증 - NPE 방어
    Map<Boolean, List<DailyChecklist>> typeGroup = checklists.stream()
        .filter(c -> c.getCareTemplate() != null && c.getCareTemplate().getCareType() != null)
        .collect(Collectors.partitioningBy(c -> isDiseaseCare(c.getCareTemplate().getCareType())));

    return CareReportResponse.builder()
        .isReportAvailable(true)
        .diseaseCareStat(calculateStat(typeGroup.get(true)))
        .basicCareStat(calculateStat(typeGroup.get(false)))
        .build();
  }

  private boolean isDiseaseCare(CareType careType) {
    return careType == CareType.DISEASE_CARE ||
        careType == CareType.MEDICATION ||
        careType == CareType.PREVENTION;
  }

  private CareReportResponse.CareCategoryStat calculateStat(List<DailyChecklist> lists) {
    if (lists == null || lists.isEmpty()) {
      return CareReportResponse.CareCategoryStat.builder().build();
    }

    Map<Long, Map<LocalDate, List<DailyChecklist>>> noteDateGrouping = lists.stream()
        .collect(Collectors.groupingBy(
            c -> c.getCareTemplate().getId(),
            Collectors.groupingBy(DailyChecklist::getTargetDate)
        ));

    int grandTotalNotes = 0;
    int grandCompletedNotes = 0;

    for (Map.Entry<Long, Map<LocalDate, List<DailyChecklist>>> noteEntry : noteDateGrouping.entrySet()) {
      Map<LocalDate, List<DailyChecklist>> dailyItems = noteEntry.getValue();

      for (List<DailyChecklist> itemsForOneDay : dailyItems.values()) {
        grandTotalNotes++;

        boolean isNoteFullyCompleted = itemsForOneDay.stream()
            .allMatch(DailyChecklist::isCompleted);

        if (isNoteFullyCompleted) {
          grandCompletedNotes++;
        }
      }
    }

    return CareReportResponse.CareCategoryStat.builder()
        .totalNoteCount(grandTotalNotes)
        .completedNoteCount(grandCompletedNotes)
        .build();
  }
}