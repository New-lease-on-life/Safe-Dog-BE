package com.newleaseonlife.SafeDogBe.domain.term.entity;

import com.newleaseonlife.SafeDogBe.domain.term.entity.enums.TermType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 약관 마스터 엔티티. 서비스 이용약관, 개인정보처리방침, 마케팅 동의 등 타입별 1건.
 */
@Entity
@Table(
        name = "terms",
        uniqueConstraints = @UniqueConstraint(name = "uk_terms_type", columnNames = "type")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Term {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 약관 종류 (SERVICE, PRIVACY, MARKETING) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermType type;

    /** 필수 동의 여부 */
    @Column(nullable = false)
    private boolean required;

    @Builder
    public Term(TermType type, boolean required) {
        this.type = type;
        this.required = required;
    }
}
