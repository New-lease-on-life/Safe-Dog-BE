package com.newleaseonlife.SafeDogBe.domain.pet.entity;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime; // createdAt, updatedAt 필드 타입
import java.util.ArrayList;
import java.util.List;

/**
 * 반려동물 엔티티. 메인 보호자(user)와 보호자 목록(guardians)을 가짐.
 * Pet 삭제 시 guardians는 cascade + orphanRemoval로 함께 삭제됨.
 */
@Entity
@Table(name = "pets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 메인 보호자(소유자). pets.user_id FK */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pets_user"))
    private User user;

    /** 보호자 목록(OWNER, CAREGIVER). Pet 삭제 시 함께 삭제, guardian 제거 시 DB에서도 삭제(orphanRemoval) */
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PetGuardian> guardians = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String species;

    @Column(length = 100)
    private String breed;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(nullable = false)
    private boolean isNeutered = false;

    @Column(length = 500)
    private String profileImageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Pet(User user, String name, String species, String breed, LocalDate birthDate,
               Gender gender, boolean isNeutered, String profileImageUrl) {
        this.user = user;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.gender = gender;
        this.isNeutered = isNeutered;
        this.profileImageUrl = profileImageUrl;
    }

    /** 정보 수정. null이 아닌 필드만 반영. updatedAt은 @LastModifiedDate가 자동 갱신 */
    public void update(String name, String species, String breed, LocalDate birthDate,
                       Gender gender, Boolean isNeutered, String profileImageUrl) {
        if (name != null) this.name = name;
        if (species != null) this.species = species;
        if (breed != null) this.breed = breed;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (isNeutered != null) this.isNeutered = isNeutered;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }
}
