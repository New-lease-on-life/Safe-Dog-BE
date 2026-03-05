package com.newleaseonlife.SafeDogBe.domain.petnote.converter;

import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.PetNote;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetNoteConverter {

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

    public List<PetNoteResponse> toResponseList(List<PetNote> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}
