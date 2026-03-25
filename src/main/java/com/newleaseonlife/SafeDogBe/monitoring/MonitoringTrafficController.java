package com.newleaseonlife.SafeDogBe.monitoring;

import com.newleaseonlife.SafeDogBe.domain.mypage.service.MypageService;
import com.newleaseonlife.SafeDogBe.domain.notification.entity.enums.NotificationType;
import com.newleaseonlife.SafeDogBe.domain.notification.service.FCMService;
import com.newleaseonlife.SafeDogBe.domain.petnote.service.ChecklistMemoService;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring/setup")
@RequiredArgsConstructor
public class MonitoringTrafficController {

  private final FCMService fcmService;
  private final MypageService mypageService;
  private final UserRepository userRepository; // 유저 객체 조회를 위해 추가

  @PostMapping("/notification")
  public String triggerNotificationTraffic(@RequestParam int count, @RequestParam Long userId) {
    // 실제 DB에 있는 유저를 사용해야 FCM 토큰 체크 로직을 통과합니다.
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

    for (int i = 0; i < count; i++) {
      // NotificationType 등 필수 파라미터를 실제와 유사하게 전달
      fcmService.sendPushNotification(user, NotificationType.SYSTEM,
          "부하테스트 " + i, "내용입니다.", "related-123");
    }
    return count + "건의 비동기 알림 요청 완료 (스레드 풀 큐 확인 필요)";
  }

  @GetMapping("/mypage")
  public String triggerMypageTraffic(@RequestParam int count, @RequestParam Long dummyUserId) {
    for (int i = 0; i < count; i++) {
      // 쿼리 파라미터(petScope)까지 포함하여 실제 API 호출 경로를 복제
      mypageService.getMypage(dummyUserId, "OWNER");
    }
    return count + "건의 마이페이지 동기 조회 완료";
  }
}