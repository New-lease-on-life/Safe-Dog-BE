package com.newleaseonlife.SafeDogBe.domain.pet.entity;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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

    public void update(String name, String species, String breed, LocalDate birthDate,
                       Gender gender, Boolean isNeutered, String profileImageUrl) {
        if (name != null) this.name = name;
        if (species != null) this.species = species;
        if (breed != null) this.breed = breed;
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (isNeutered != null) this.isNeutered = isNeutered;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }
}
