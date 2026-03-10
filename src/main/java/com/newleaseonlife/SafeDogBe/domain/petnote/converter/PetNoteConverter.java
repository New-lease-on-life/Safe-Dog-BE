package com.newleaseonlife.SafeDogBe.domain.petnote.converter;

import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * PetNote 엔티티 → PetNoteResponse 변환. API 응답 DTO 생성 시 사용.
 */
@Component
public class PetNoteConverter {

    /** 엔티티를 응답 DTO로 변환. petId는 getPetId() 편의 메서드로 조회 */
    public PetNoteResponse toResponse(PetNote entity) {
        return PetNoteResponse.builder()
                .id(entity.getId())
                .petId(entity.getPetId())
                .noteDate(entity.getNoteDate())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /** 엔티티 목록을 응답 DTO 목록으로 변환 */
    public List<PetNoteResponse> toResponseList(List<PetNote> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
