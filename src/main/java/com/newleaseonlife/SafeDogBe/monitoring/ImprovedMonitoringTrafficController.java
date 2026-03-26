package com.newleaseonlife.SafeDogBe.monitoring;

import static java.time.LocalDate.now;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.enums.ProviderType;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.entity.enums.CareType;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.mypage.service.MypageService;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.newleaseonlife.SafeDogBe.domain.notification.service.FCMService;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.ChecklistMemoRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.service.ChecklistMemoService;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserRole;
import com.newleaseonlife.SafeDogBe.domain.user.entity.enums.UserStatus;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring/setup")
@RequiredArgsConstructor
@Slf4j
public class ImprovedMonitoringTrafficController {

  private final FCMService fcmService;
  private final MypageService mypageService;
  private final UserRepository userRepository;
  private final PetRepository petRepository;
  private final PetGuardianRepository petGuardianRepository;
  private final ChecklistMemoService checklistMemoService;
  private final DailyChecklistRepository dailyChecklistRepository;

  /**
   * 📌 알림 발송 성능 테스트
   * <p>
   * 필수 조건: 데이터베이스에 실제 User가 존재해야 함 - 로그인: ❌ 불필요 (userId를 직접 전달) - 펫 등록: ❌ 불필요 (FCM 토큰만 필요)
   * <p>
   * 주의: 실제 FCMService.sendPushNotification()이 내부적으로 User 객체의 fcmToken을 사용하는지 확인 필수!
   */
  @PostMapping("/notification")
  public String triggerNotificationTraffic(
      @RequestParam int count,
      @RequestParam Long userId) {

    log.info("🔔 알림 성능 테스트 시작: count={}, userId={}", count, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            "❌ 존재하지 않는 유저입니다. 먼저 DB에 유저 생성 필요"));

    // FCM 토큰이 없으면 발송 실패 가능
    if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
      return "⚠️ 유저 " + userId + "의 FCM 토큰이 없습니다. " +
          "앱에서 로그인 후 FCM 토큰 저장 필요";
    }

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < count; i++) {
      fcmService.sendPushNotification(user, NotificationType.SYSTEM,
          "부하테스트 " + i, "내용입니다.", "related-123");
    }

    long duration = System.currentTimeMillis() - startTime;
    log.info("✅ 알림 성능 테스트 완료: {}ms 소요", duration);

    return count + "건의 알림 요청 완료 (" + duration + "ms)";
  }

  /**
   * 📌 마이페이지 조회 성능 테스트
   * <p>
   * 필수 조건: 데이터베이스에 User가 존재해야 함 - 로그인: ❌ 불필요 - 펫 등록: ❓ MypageService 구현에 따라 다름 (펫이 없어도 빈 리스트 반환하면
   * 불필요, 필수 검증하면 필요)
   * <p>
   * ⚠️ 권장: getMypage() 메서드의 내부 로직을 확인하고 펫 필수 여부를 판단한 후 이 메서드 사용
   */
  @GetMapping("/mypage")
  public String triggerMypageTraffic(
      @RequestParam int count,
      @RequestParam Long userId) {

    log.info("📄 마이페이지 성능 테스트 시작: count={}, userId={}", count, userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            "❌ 존재하지 않는 유저입니다."));

    long startTime = System.currentTimeMillis();

    for (int i = 0; i < count; i++) {
      try {
        mypageService.getMypage(userId, "OWNER");
      } catch (Exception e) {
        log.warn("⚠️ 마이페이지 조회 실패 ({}번째): {}", i, e.getMessage());
        // 펫이 필수인 경우 여기서 예외 발생
      }
    }

    long duration = System.currentTimeMillis() - startTime;
    log.info("✅ 마이페이지 성능 테스트 완료: {}ms 소요", duration);

    return count + "건의 마이페이지 조회 완료 (" + duration + "ms)";
  }

  /**
   * 📌 체크리스트 메모 생성 성능 테스트
   * <p>
   * 필수 조건: 데이터베이스에 User와 Pet이 모두 존재해야 함 - 로그인: ❌ 불필요 - 펫 등록: ✅ 필수!
   * <p>
   * ChecklistMemoService가 petId를 요구하기 때문에 사전에 펫을 생성해야 합니다.
   */
  @PostMapping("/checklist-memo")
  public String triggerChecklistMemoTraffic(
      @RequestParam int count,
      @RequestParam Long userId,
      @RequestParam Long checklistId) {

    log.info("✍️ 메모 성능 테스트 시작: count={}, userId={}, checklistId={}",
        count, userId, checklistId);

    userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException(
            "❌ 존재하지 않는 유저입니다."));

    long startTime = System.currentTimeMillis();

    ChecklistMemoRequest request =
        new ChecklistMemoRequest("부하 테스트 메모 내용");

    for (int i = 0; i < count; i++) {
      try {
        // ChecklistMemoService의 실제 메서드 시그니처에 맞게 수정 필요
        checklistMemoService.createMemo(checklistId, userId, request);
      } catch (Exception e) {
        log.warn("⚠️ 메모 생성 실패 ({}번째): {}", i, e.getMessage());
      }
    }

    long duration = System.currentTimeMillis() - startTime;
    log.info("✅ 메모 성능 테스트 완료: {}ms 소요", duration);

    return count + "건의 메모 생성 완료 (" + duration + "ms)";
  }

  // ImprovedMonitoringTrafficController.java 수정
  @PostMapping("/checklist-memo/bulk")
  public String triggerChecklistMemoBulkTraffic(@RequestParam int count, @RequestParam Long userId,
      @RequestParam Long checklistId) {
    log.info("✍️ 메모 성능 테스트 시작: count={}, userId={}, checklistId={}",
        count, userId, checklistId);

    long startTime = System.currentTimeMillis();
    List<String> contents = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      contents.add("부하 테스트 메모 " + i);
    }

    // 서비스를 딱 '한 번' 호출합니다.
    checklistMemoService.createMemosBulk(checklistId, userId, contents);

    long duration = System.currentTimeMillis() - startTime;
    log.info("✅ 메모 성능 테스트 완료: {}ms 소요", duration);

    return count + "건의 벌크 메모 생성 완료 (" + duration + "ms)";
  }

  /**
   * 📌 부하 테스트용 슈퍼 유저 강제 생성 API (All-in-One) - 유저 생성 (ACTIVE 상태) - FCM 토큰 강제 주입 - 펫 강제 생성 - 유저 <-> 펫
   * 보호자(OWNER) 권한 강제 매핑
   */
  @PostMapping("/create-perfect-user")
  @org.springframework.transaction.annotation.Transactional
  public java.util.Map<String, Object> createPerfectTestUser() {

    // 1. 유저 생성 및 FCM 토큰 주입
    String randomStr = java.util.UUID.randomUUID().toString().substring(0, 5);
    User user = User.builder()
        .email("loadtest_" + randomStr + "@test.com")
        .nickname("부하테스터_" + randomStr)
        .status(UserStatus.ACTIVE)
        .role(UserRole.USER)
        .providerType(ProviderType.LOCAL)
        .build();
    user.updateFcmToken("dummy-fcm-token-" + randomStr);
    user = userRepository.save(user);

    java.util.List<Long> petIds = new java.util.ArrayList<>();
    Pet lastCreatedPet = null; // 체크리스트 생성을 위해 마지막 펫 저장

    // 2. 펫 강제 생성
    for (int i = 0; i < 50; i++) {
      Pet pet = Pet.builder()
          .user(user)
          .name("테스트멍멍이_" + randomStr)
          .species(SpeciesType.DOG)
          .gender(Gender.MALE)
          .birthDate(now().minusYears(2)) // 2살
          .build();

      lastCreatedPet = petRepository.save(pet);
      petIds.add(lastCreatedPet.getId());

      // 3. 보호자(OWNER) 매핑 (마이페이지 및 메모 권한 통과용)
      PetGuardian guardian =
          PetGuardian.builder()
              .user(user)
              .pet(pet)
              .role(PetGuardianRole.OWNER)
              .build();
      petGuardianRepository.save(guardian);
      log.info("[테스트 유저 생성] {}번째 펫 및 보호자 등록 완료: ID {}", i, lastCreatedPet.getId());
    }
    DailyChecklist checklist =
        dailyChecklistRepository.save(DailyChecklist.builder()
            .pet(lastCreatedPet)
            .targetDate(java.time.LocalDate.now()) // 명시적 타입 지정
            .careType(CareType.ETC)                // category -> careType으로 수정
            .title("모니터링용 가짜 할일")           // taskName -> title로 수정
            .content("부하 테스트를 위한 자동 생성 데이터입니다.") // 생성자에 포함된 필드
            .build());

    // 4. 테스트하기 편하도록 ID값들을 JSON으로 반환

    java.util.Map<String, Object> result = new java.util.LinkedHashMap<>(); // 순서 보장을 위해 LinkedHashMap 사용
    result.put("message", "✅ 테스트 유저 및 데이터 세팅 완료");
    result.put("userId", user.getId());
    result.put("email", user.getEmail());
    result.put("fcmToken", user.getFcmToken());
    result.put("createdPetCount", petIds.size());
    result.put("petIds", petIds); // 생성된 모든 펫 ID 반환
    result.put("checklistId", checklist.getId());
    result.put("message", "✅ 모든 조건을 만족하는 테스트 유저 세팅 완료!");

    return result;
  }
}
