package com.newleaseonlife.SafeDogBe.domain.petnote.entity;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.global.common.BaseTimeEntity;

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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 날짜별 반려동물 기록 (반려노트). Pet 1 : N PetNote.
 */
@Entity
@Table(name = "pet_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 대상 반려동물. FK 이름 fk_pet_notes_pet */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pet_notes_pet"))
    private Pet pet;

    /** 기록 대상일. 날짜별 조회에 사용 */
    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    /** 메모 내용. TEXT 타입 */
    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public PetNote(Pet pet, LocalDate noteDate, String content) {
        this.pet = pet;
        this.noteDate = noteDate;
        this.content = content;
    }

    /** 편의 메서드: petId가 필요한 경우를 위해 제공 */
    public Long getPetId() {
        return pet != null ? pet.getId() : null;
    }

    /** 내용·기록일 수정. null이 아닌 값만 반영 */
    public void update(String content, LocalDate noteDate) {
        if (content != null) this.content = content;
        if (noteDate != null) this.noteDate = noteDate;
    }
}
