package com.newleaseonlife.SafeDogBe.domain.pet.entity;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/**
 * 반려동물-보호자 연결. 한 반려동물에 여러 보호자(OWNER, CAREGIVER) 지정 가능.
 * Pet 엔티티에서 @OneToMany(guardians)로 역방향 참조.
 */
@Entity
@Table(
        name = "pet_guardian",
        uniqueConstraints = @UniqueConstraint(name = "uk_pet_guardian_user_pet", columnNames = {"user_id", "pet_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetGuardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보호자로 등록된 회원 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 대상 반려동물. Pet.guardians와 양방향 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /** 역할. OWNER 또는 CAREGIVER */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PetGuardianRole role;

    @Builder
    public PetGuardian(User user, Pet pet, PetGuardianRole role) {
        this.user = user;
        this.pet = pet;
        this.role = role;
    }
}
