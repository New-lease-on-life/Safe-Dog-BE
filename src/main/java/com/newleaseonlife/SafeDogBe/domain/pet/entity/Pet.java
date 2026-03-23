package com.newleaseonlife.SafeDogBe.domain.pet.entity;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.Gender;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetDisease;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 수정 3월 18일 반려동물 엔티티.
 * <p>
 * ✅ 추가: weight, isWeightUnknown, registrationNumber, isBirthDateUnknown, hasAllergy,
 * allergyDescription ✅ 제거: PetDisease.ALLERGY → hasAllergy/allergyDescription 필드로 분리
 * <p>
 * ⚠️ 단방향 원칙: - [기존 리팩토링 문서 오류] guardians(@OneToMany) 필드가 있었음 → 제거 - PetGuardian → Pet 단방향
 *
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

  /**
   * 반려 동물 이름
   */
  @Column(nullable = false, length = 100)
  private String name;

  /**
   * 출생일
   */
  private LocalDate birthDate;

  /**
   * 출생일을 모르는 경우 true. true이면 birthDate는 null
   */
  @Column(nullable = false)
  private boolean isBirthDateUnknown = false;

  /**
   * 체중 (kg). 소수점 1자리까지 허용. isWeightUnknown=true이면 null 허용.
   */
  @Column(precision = 5, scale = 1)
  private BigDecimal weight;

  /**
   * 체중을 모르는 경우 true. true이면 weight는 null
   */
  @Column(nullable = false)
  private boolean isWeightUnknown = false;

//------------stem 2--------------
  /**
   * 종 (강아지/고양이) 구분
   */
  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false)
  private SpeciesType species;

  /**
   * 품종 코드 (DogBreed, CatBreed 등 공통으로 아우를 수 있는 식별자, 혹은 String 그대로 유지) Enum을 분리해 두셨으므로, String으로 받되
   * 서비스 단에서 Enum 검증을 하는 것이 좋습니다. 예: "MALTESE", "KOREAN_SHORTHAIR", "ETC"
   */
  @Column(length = 50)
  private String breedCode; // 명확성을 위해 breedCode로 변경 고려

  /**
   * 사용자가 직접 입력한 품종 이름 (breedCode가 ETC일 경우 사용, 그 외는 Enum의 description 저장) 예: "말티푸", "코리안 숏헤어"
   */
  @Column(length = 100)
  private String breedName;

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

  /**
   * 알레르기 여부. 기획서 step3 알레르기 '있어요/없어요'
   */
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
  public Pet(User user, String name, SpeciesType species, String breedCode, String breedName,
      LocalDate birthDate, boolean isBirthDateUnknown,
      Gender gender, boolean isNeutered,
      BigDecimal weight, boolean isWeightUnknown,
      String registrationNumber,
      Boolean hasAllergy, String allergyDescription,
      String profileImageUrl, Set<PetDisease> diseases) {
    this.user = user;
    this.name = name;
    this.species = species;
    this.breedCode = breedCode; // Enum 코드값 (예: MALTESE, ETC)
    this.breedName = breedName; // 화면 노출용 또는 직접 입력값 (예: 말티즈, 말티푸)
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
  public void update(String name, SpeciesType species, String breedCode, String breedName,
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
    if (breedCode != null) {
      this.breedCode = breedCode;
    }
    if (breedName != null) {
      this.breedName = breedName;
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
    if (diseases != null) {
      this.diseases.addAll(diseases);
    }
  }

  /**
   * 관리자(메인 보호자) 변경을 위한 소유자 업데이트
   */
  public void changeOwner(User newOwner) {
    this.user = newOwner;
  }
}