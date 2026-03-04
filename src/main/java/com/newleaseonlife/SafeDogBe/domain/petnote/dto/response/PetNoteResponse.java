package com.newleaseonlife.SafeDogBe.domain.petnote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNoteResponse {

    private Long id;
    private Long petId;
    private LocalDate noteDate;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
