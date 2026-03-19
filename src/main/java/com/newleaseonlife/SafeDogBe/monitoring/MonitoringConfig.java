package com.newleaseonlife.SafeDogBe.monitoring;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 모니터링 인프라 설정.
 * <ul>
 *   <li>{@link TimedAspect} – 서비스 메서드에 {@code @Timed} 어노테이션만 붙이면 자동 타이머 등록</li>
 *   <li>{@link MeterRegistryCustomizer} – 전체 메트릭에 공통 태그 삽입 (Grafana 필터용)</li>
 * </ul>
 */
@Configuration
@ConditionalOnWebApplication
public class MonitoringConfig {

    /**
     * @Timed 어노테이션 활성화.
     * 서비스 메서드에 @Timed(value = "safedog.xxx", description = "...") 추가 시
     * Prometheus에서 히스토그램·평균·최대 응답 시간 등 자동 수집.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * 전체 메트릭에 공통 태그 {application="SafeDogBe"} 를 삽입.
     * Grafana에서 application 기준으로 필터링 가능.
     *
     * <p><b>중복 주의</b><br>
     * application-prod.yaml 에 {@code management.metrics.tags.application: SafeDogBe} 가 이미 선언되어 있어
     * prod 프로필에서는 같은 태그가 두 경로로 등록된다.
     * Micrometer는 동일 태그를 중복 적용해도 결과가 같으므로 런타임 오류는 없지만,
     * local/test 환경처럼 prod yaml이 로드되지 않는 경우에는 이 빈이 유일한 태그 공급원이 된다.
     * prod yaml 설정을 제거하거나 이 빈을 제거하면 어느 한쪽에서 태그가 빠지므로 의도적으로 둘 다 유지.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTagsCustomizer() {
        return registry -> registry.config()
                .meterFilter(MeterFilter.commonTags(
                        List.of(Tag.of("application", "SafeDogBe"))
                ));
    }
}
