package com.newleaseonlife.SafeDogBe.monitoring.metrics;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 모니터링 전용 DB 집계 서비스.
 * Gauge 등록 시 람다로 참조되어 Prometheus 스크랩 시점에 실시간 쿼리를 실행.
 * 기존 Repository 메서드로 불가능한 집계는 JPQL로 직접 처리.
 */
@Component
public class SafeDogMetricsService {

    @PersistenceContext
    private EntityManager entityManager;

    /** 현재 활성화된 케어 템플릿 수 */
    @Transactional(readOnly = true)
    public long countActiveCareTemplates() {
        return entityManager.createQuery(
                        "SELECT COUNT(ct) FROM CareTemplate ct WHERE ct.isActive = true", Long.class)
                .getSingleResult();
    }

    /** 오늘 날짜의 전체 체크리스트 수 */
    @Transactional(readOnly = true)
    public long countTodayChecklists() {
        return entityManager.createQuery(
                        "SELECT COUNT(d) FROM DailyChecklist d WHERE d.targetDate = :today", Long.class)
                .setParameter("today", LocalDate.now())
                .getSingleResult();
    }

    /** 오늘 날짜에 완료된 체크리스트 수 */
    @Transactional(readOnly = true)
    public long countTodayCompletedChecklists() {
        return entityManager.createQuery(
                        "SELECT COUNT(d) FROM DailyChecklist d WHERE d.isCompleted = true AND d.targetDate = :today", Long.class)
                .setParameter("today", LocalDate.now())
                .getSingleResult();
    }
}
