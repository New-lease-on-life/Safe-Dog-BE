package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.ChecklistHistoryLogRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/** 3월 18일 수정
 * ✅ 변경: RepeatCycle.DAILY 필터 제거 → CareTemplate.shouldGenerateToday()로 주기 판단
 * ✅ 추가: 로그 90일 자동 삭제 배치
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareSchedulerService {

  private final CareTemplateRepository careTemplateRepository;
  private final DailyChecklistRepository dailyChecklistRepository;
  private final ChecklistHistoryLogRepository historyLogRepository;

  /**
   * 매일 자정(KST 00:00:00) 일일 체크리스트 자동 생성.
   *
   * ✅ 변경: findAllActiveTemplatesByCycleWithPet(DAILY) → findAllActiveTemplatesWithPet()
   *   각 템플릿의 shouldGenerateToday()로 오늘 생성 여부 판단
   */
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  @Transactional
  public void generateDailyChecklists() {
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    log.info("[Batch Start] {} 일자 DailyChecklist 자동 생성 시작", today);

    List<CareTemplate> allTemplates = careTemplateRepository.findAllActiveTemplatesWithPet();
    List<DailyChecklist> toSave = new ArrayList<>();

    for (CareTemplate template : allTemplates) {
      // 오늘 이 템플릿을 생성해야 하는지 주기 판단
      if (!template.shouldGenerateToday(today)) continue;

      // 중복 생성 방지
      if (dailyChecklistRepository.existsByCareTemplateIdAndTargetDate(template.getId(), today)) {
        continue;
      }

      toSave.add(DailyChecklist.builder()
          .pet(template.getPet())
          .careTemplate(template)
          .targetDate(today)
          .careType(template.getCareType())
          .title(template.getTitle())
          .content(template.getMemo())
          .build());
    }

    if (!toSave.isEmpty()) {
      dailyChecklistRepository.saveAll(toSave);
      log.info("[Batch Success] {}개 DailyChecklist 생성 완료", toSave.size());
    } else {
      log.info("[Batch End] 생성할 DailyChecklist 없음");
    }
  }

  /**
   * 매일 새벽 1시: 90일 초과 로그 삭제 (기획서 3: 로그 90일 보관)
   */
  @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
  @Transactional
  public void deleteOldLogs() {
    LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(90);
    int deleted = historyLogRepository.deleteOldLogs(cutoff);
    log.info("[Batch] 90일 초과 체크리스트 로그 {}건 삭제 완료", deleted);
  }
}