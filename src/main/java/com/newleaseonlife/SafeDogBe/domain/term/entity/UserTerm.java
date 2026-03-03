package com.newleaseonlife.SafeDogBe.domain.term.entity;

import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Entity
@Table(name = "user_terms",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "term_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Column(nullable = false)
    private boolean agreed;

    private LocalDateTime agreedAt;

    @Builder
    public UserTerm(User user, Term term, boolean agreed) {
        this.user = user;
        this.term = term;
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }

    public void updateAgreed(boolean agreed) {
        this.agreed = agreed;
        this.agreedAt = agreed ? LocalDateTime.now() : null;
    }
}
