package com.newleaseonlife.SafeDogBe.domain.pet.entity;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 수정 3월 18일 반려동물 엔티티.
 * <p>
 * ✅ 추가: weight, isWeightUnknown, registrationNumber, isBirthDateUnknown, hasAllergy,
 * allergyDescription ✅ 제거: PetDisease.ALLERGY → hasAllergy/allergyDescription 필드로 분리
 * <p>
 * ⚠️ 단방향 원칙: - [기존 리팩토링 문서 오류] guardians(@OneToMany) 필드가 있었음 → 제거 - PetGuardian → Pet 단방향
 * @ManyToOne 유지 - 보호자 조회/삭제는 PetGuardianRepository에서만 처리
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

  /**
   * 메인 보호자(소유자). Pet → User 단방향
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pets_user"))
  private User user;

  //--------------step 1---------------------------

  @Column(columnDefinition = "TEXT")
  private String profileImageUrl;

  /** 반려 동물 이름 */
  @Column(nullable = false, length = 100)
  private String name;

  /**출생일*/
  private LocalDate birthDate;

  /** 출생일을 모르는 경우 true. true이면 birthDate는 null */
  @Column(nullable = false)
  private boolean isBirthDateUnknown = false;

  /** 체중 (kg). 소수점 1자리까지 허용. isWeightUnknown=true이면 null 허용.*/
  @Column(precision = 5, scale = 1)
  private BigDecimal weight;

  /** 체중을 모르는 경우 true. true이면 weight는 null */
  @Column(nullable = false)
  private boolean isWeightUnknown = false;

//------------stem 2--------------
  /** 종 (강아지/고양이) 구분  */
  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private SpeciesType species;

  // TODO: 바텀 시트는 어떻게 처리가 되는것인가?
  @Column(length = 100)
  private String breed;

  @Enumerated(EnumType.STRING)
  @Column(length = 10)
  private Gender gender;

  @Column(nullable = false)
  private boolean isNeutered = false;

  /**
   * 동물등록번호. 숫자만, 최대 15자리. 선택 입력.
   */
  @Column(length = 15)
  private String registrationNumber;

  //----------step 3---------
  /**
   * 질병 목록. 최대 5개. pet_diseases 별도 테이블. ElementCollection은 Pet → 단방향이므로 원칙 준수.
   */
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(
      name = "pet_diseases",
      joinColumns = @JoinColumn(name = "pet_id", foreignKey = @ForeignKey(name = "fk_pet_diseases_pet"))
  )
  @Enumerated(EnumType.STRING)
  @Column(name = "disease", length = 30, nullable = false)
  private Set<PetDisease> diseases = new HashSet<>();

  /** 알레르기 여부. 기획서 step3 알레르기 '있어요/없어요'  */
  private Boolean hasAllergy;

  /**
   * 알레르기 직접 입력 내용. hasAllergy=true일 때만 사용.
   */
  @Column(columnDefinition = "TEXT")
  private String allergyDescription;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  public Pet(User user, String name, SpeciesType species, String breed,
      LocalDate birthDate, boolean isBirthDateUnknown,
      Gender gender, boolean isNeutered,
      BigDecimal weight, boolean isWeightUnknown,
      String registrationNumber,
      Boolean hasAllergy, String allergyDescription,
      String profileImageUrl, Set<PetDisease> diseases) {
    this.user = user;
    this.name = name;
    this.species = species;
    this.breed = breed;
    this.birthDate = birthDate;
    this.isBirthDateUnknown = isBirthDateUnknown;
    this.gender = gender;
    this.isNeutered = isNeutered;
    this.weight = weight;
    this.isWeightUnknown = isWeightUnknown;
    this.registrationNumber = registrationNumber;
    this.hasAllergy = hasAllergy;
    this.allergyDescription = allergyDescription;
    this.profileImageUrl = profileImageUrl;
      if (diseases != null) {
          this.diseases = diseases;
      }
  }

  /**
   * 정보 수정. null이 아닌 필드만 반영
   */
  public void update(String name, SpeciesType species, String breed,
      LocalDate birthDate, Boolean isBirthDateUnknown,
      Gender gender, Boolean isNeutered,
      BigDecimal weight, Boolean isWeightUnknown,
      String registrationNumber,
      Boolean hasAllergy, String allergyDescription,
      String profileImageUrl) {
      if (name != null) {
          this.name = name;
      }
      if (species != null) {
          this.species = species;
      }
      if (breed != null) {
          this.breed = breed;
      }
      if (birthDate != null) {
          this.birthDate = birthDate;
      }
      if (isBirthDateUnknown != null) {
          this.isBirthDateUnknown = isBirthDateUnknown;
      }
      if (gender != null) {
          this.gender = gender;
      }
      if (isNeutered != null) {
          this.isNeutered = isNeutered;
      }
      if (weight != null) {
          this.weight = weight;
      }
      if (isWeightUnknown != null) {
          this.isWeightUnknown = isWeightUnknown;
      }
      if (registrationNumber != null) {
          this.registrationNumber = registrationNumber;
      }
      if (hasAllergy != null) {
          this.hasAllergy = hasAllergy;
      }
      if (allergyDescription != null) {
          this.allergyDescription = allergyDescription;
      }
      if (profileImageUrl != null) {
          this.profileImageUrl = profileImageUrl;
      }
  }

  /**
   * 질병 목록 수정
   */
  public void updateDiseases(Set<PetDisease> diseases) {
    this.diseases.clear();
    if (diseases != null)
      this.diseases.addAll(diseases);
  }

  /** 관리자(메인 보호자) 변경을 위한 소유자 업데이트 */
  public void changeOwner(User newOwner) {
    this.user = newOwner;
  }
}