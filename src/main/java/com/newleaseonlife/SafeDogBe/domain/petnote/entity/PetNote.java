package com.newleaseonlife.SafeDogBe.domain.petnote.entity;

import com.newleaseonlife.SafeDogBe.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 날짜별 반려동물 기록 (반려노트).
 * Pet 1 : N PetNote.
 *
 * 추후 연결: Pet 도메인 머지 후 아래를 적용할 예정.
 * - petId 대신 @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "pet_id") private Pet pet;
 * - getPetId()는 pet != null ? pet.getId() : null 로 제공하거나, pet 필드로 통일.
 */
@Entity
@Table(name = "pet_notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetNote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 반려동물 ID. 추후 Pet 도메인 머지 시 @ManyToOne Pet pet 로 교체 후 연관관계 매핑 */
    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder
    public PetNote(Long petId, LocalDate noteDate, String content) {
        this.petId = petId;
        this.noteDate = noteDate;
        this.content = content;
    }

    public void update(String content) {
        this.content = content;
    }
}
