package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplate;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.RepeatCycle;
import com.newleaseonlife.SafeDogBe.domain.care.repository.CareTemplateRepository;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j // 콘솔에 배치 결과를 찍기 위한 로깅 어노테이션
@Service
@RequiredArgsConstructor
public class CareSchedulerService {

  private final CareTemplateRepository careTemplateRepository;
  private final DailyChecklistRepository dailyChecklistRepository;

  /**
   * 매일 자정(KST 00:00:00)에 실행되는 자동 체크리스트 생성 배치
   * cron = "초 분 시 일 월 요일"
   */
  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  @Transactional
  public void generateDailyChecklists() {
    // 1. 기획서 기준대로 시스템 시간이 아닌 철저히 KST(한국 시간) 기준의 오늘 날짜 획득
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
    log.info("[Batch Start] {} 일자 반려노트 DailyChecklist 자동 생성 시작", today);

    // 2. '매일(DAILY)' 반복 설정이 되어 있으면서 비활성화되지 않은(isActive=true) 템플릿 목록 싹쓸이 (N+1 방지 적용)
    List<CareTemplate> dailyTemplates = careTemplateRepository.findAllActiveTemplatesByCycleWithPet(RepeatCycle.DAILY);

    // 저장을 위해 모아둘 리스트 (건바이건 Insert 방지)
    List<DailyChecklist> checklistsToSave = new ArrayList<>();

    for (CareTemplate template : dailyTemplates) {
      // 3. 방어 로직: 스케줄러가 모종의 이유로 두 번 돌더라도, 오늘 날짜로 이미 생성된 할 일이 있다면 건너뜀 (중복 생성 방지)
      boolean isAlreadyGenerated = dailyChecklistRepository.existsByCareTemplateIdAndTargetDate(template.getId(), today);

      if (!isAlreadyGenerated) {
        // 4. 원본 템플릿의 '현재' 텍스트를 그대로 복사(스냅샷)하여 새로운 엔티티 조립
        DailyChecklist newChecklist = DailyChecklist.builder()
            .pet(template.getPet())
            .careTemplate(template)
            .targetDate(today)
            .careType(template.getCareType())
            .title(template.getTitle())
            .content(template.getContent())
            .build();

        checklistsToSave.add(newChecklist);
      }
    }

    // 5. 모아둔 리스트를 한 번의 쿼리(Batch Insert)로 DB에 꽂아 넣음 (성능 최적화)
    if (!checklistsToSave.isEmpty()) {
      dailyChecklistRepository.saveAll(checklistsToSave);
      log.info("[Batch Success] 총 {}개의 DailyChecklist가 성공적으로 자동 생성되었습니다.", checklistsToSave.size());
    } else {
      log.info("[Batch End] 오늘 날짜로 새로 생성할 DailyChecklist가 없습니다.");
    }
  }
}