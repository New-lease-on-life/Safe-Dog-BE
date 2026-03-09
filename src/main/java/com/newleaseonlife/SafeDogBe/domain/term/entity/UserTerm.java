package com.newleaseonlife.SafeDogBe.domain.term.entity;

import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 회원-약관 동의 내역 엔티티.
 * (user_id, term_id) 유니크로 한 사용자당 약관별 동의 이력 1건.
 * agreedAt은 동의 시각(동의 시에만 기록).
 */
@Entity
@Table(
        name = "user_terms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_terms_user_term",
                columnNames = {"user_id", "term_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 동의한 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_terms_user")
    )
    private User user;

    /** 대상 약관 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "term_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_terms_term")
    )
    private Term term;

    /** 동의 여부. 기본값 false */
    @Column(nullable = false)
    private boolean agreed = false;

    /** 동의 시각. 동의 시에만 기록 */
    @Column(name = "agreed_at")
    private LocalDateTime agreedAt;

    @Builder
    public UserTerm(User user, Term term, boolean agreed) {
        this.user = user;
        this.term = term;
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }

    /** 동의 여부 변경. agreed true 시 agreedAt을 현재 시각으로 설정, false 시 null. */
    public void updateAgreed(boolean agreed) {
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }
}
