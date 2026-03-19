package com.newleaseonlife.SafeDogBe.monitoring.metrics;

import com.newleaseonlife.SafeDogBe.domain.auth.repository.UserDeviceRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 모니터링 패키지 전용 Gauge 메트릭 설정.
 * 람다 참조이므로 Prometheus가 스크랩할 때마다 실시간으로 DB에서 최신 값을 가져옴.
 * <p>
 * Grafana 권장 메트릭:
 * <ul>
 *   <li>safedog.users.total          – 가입자 수 추이</li>
 *   <li>safedog.pets.total           – 등록 반려동물 수</li>
 *   <li>safedog.guardians.total      – 공동보호자 관계 수</li>
 *   <li>safedog.devices.registered   – 기기 등록 수 (로그인 기기 관리)</li>
 *   <li>safedog.care.templates.active – 현재 활성 케어 템플릿 수</li>
 *   <li>safedog.checklist.today.total     – 오늘 생성된 할 일 수</li>
 *   <li>safedog.checklist.today.completed – 오늘 완료된 할 일 수</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationMetricsConfig {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PetGuardianRepository petGuardianRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final SafeDogMetricsService metricsService;

    @Bean
    public MeterBinder safeDogMeterBinder() {
        return registry -> {

            Gauge.builder("safedog.users.total", userRepository, UserRepository::count)
                    .description("Total registered users")
                    .register(registry);

            Gauge.builder("safedog.pets.total", petRepository, PetRepository::count)
                    .description("Total registered pets")
                    .register(registry);

            Gauge.builder("safedog.guardians.total", petGuardianRepository, PetGuardianRepository::count)
                    .description("Total pet-guardian relationships (including co-guardians)")
                    .register(registry);

            Gauge.builder("safedog.devices.registered", userDeviceRepository, UserDeviceRepository::count)
                    .description("Total registered login devices")
                    .register(registry);

            Gauge.builder("safedog.care.templates.active", metricsService, SafeDogMetricsService::countActiveCareTemplates)
                    .description("Currently active care templates")
                    .register(registry);

            Gauge.builder("safedog.checklist.today.total", metricsService, SafeDogMetricsService::countTodayChecklists)
                    .description("Total checklists for today")
                    .register(registry);

            Gauge.builder("safedog.checklist.today.completed", metricsService, SafeDogMetricsService::countTodayCompletedChecklists)
                    .description("Completed checklists for today")
                    .register(registry);
        };
    }
}
