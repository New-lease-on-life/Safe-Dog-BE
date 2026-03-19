package com.newleaseonlife.SafeDogBe.monitoring.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 실헬스 체크.
 * 단순 "빈 존재 여부"가 아니라 실제 ping을 날려 응답 가능 여부를 판단한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisPingHealthIndicator implements HealthIndicator {

    private final StringRedisTemplate redisTemplate;

    @Override
    public Health health() {
        try {
            RedisConnectionFactory connectionFactory = redisTemplate.getConnectionFactory();
            if (connectionFactory == null) {
                return Health.down()
                        .withDetail("redis", "DOWN")
                        .withDetail("reason", "RedisConnectionFactory is null")
                        .build();
            }

            String pong = connectionFactory.getConnection().ping();

            boolean ok = "PONG".equalsIgnoreCase(pong);
            return ok
                    ? Health.up().withDetail("redis", "UP").build()
                    : Health.down().withDetail("redis", "DOWN").withDetail("ping", pong).build();
        } catch (Exception e) {
            log.error("[HealthCheck] Redis ping 실패: {}", e.getMessage());
            return Health.down()
                    .withDetail("redis", "DOWN")
                    .withException(e)
                    .build();
        }
    }
}

