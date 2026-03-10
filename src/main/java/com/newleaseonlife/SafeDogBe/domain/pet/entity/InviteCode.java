package com.newleaseonlife.SafeDogBe.domain.pet.entity;

import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공동 보호자 초대 코드. 반려동물 OWNER가 생성하며 만료 기간 내 1회 사용 가능.
 * 만료(expiredAt) 또는 사용 완료(isUsed=true)이면 무효.
 */
@Entity
@Table(name = "invite_codes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 초대 코드 문자열. UUID 기반 8자리 대문자 알파뉴메릭.
     * 유니크 제약: invite_codes.code
     */
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    /** 초대 대상 반려동물 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_codes_pet"))
    private Pet pet;

    /** 초대 코드를 생성한 보호자(OWNER) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_codes_inviter"))
    private User inviter;

    /** 초대 코드 만료 일시. 이 시간 이후에는 사용 불가 */
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    /** 사용 완료 여부. 한 코드는 1회만 사용 가능 */
    @Column(nullable = false)
    private boolean isUsed = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public InviteCode(String code, Pet pet, User inviter, LocalDateTime expiredAt) {
        this.code = code;
        this.pet = pet;
        this.inviter = inviter;
        this.expiredAt = expiredAt;
        this.isUsed = false;
    }

    /** 초대 코드 사용 처리. isUsed = true */
    public void use() {
        this.isUsed = true;
    }

    /** 만료 또는 사용 완료 여부 */
    public boolean isExpiredOrUsed(LocalDateTime now) {
        return isUsed || now.isAfter(expiredAt);
    }
}
