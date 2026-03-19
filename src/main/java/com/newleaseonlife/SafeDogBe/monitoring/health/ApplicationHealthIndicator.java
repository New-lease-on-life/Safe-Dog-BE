package com.newleaseonlife.SafeDogBe.monitoring.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 애플리케이션 수준 헬스 체크.
 * DB 커넥션 획득을 직접 시도해 "서비스가 실제로 살아있는지" 판단.
 * /actuator/health 응답에 "application" 항목으로 포함됨.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final String APPLICATION_NAME = "SafeDogBe";

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            return Health.up()
                    .withDetail("application", APPLICATION_NAME)
                    .withDetail("database", "UP")
                    .withDetail("dbProductName", conn.getMetaData().getDatabaseProductName())
                    .withDetail("dbVersion", conn.getMetaData().getDatabaseProductVersion())
                    .build();
        } catch (Exception e) {
            log.error("[HealthCheck] DB 연결 실패: {}", e.getMessage());
            return Health.down()
                    .withDetail("application", APPLICATION_NAME)
                    .withDetail("database", "DOWN")
                    .withException(e)
                    .build();
        }
    }
}
