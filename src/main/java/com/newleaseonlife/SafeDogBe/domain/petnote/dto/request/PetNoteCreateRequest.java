package com.newleaseonlife.SafeDogBe.domain.petnote.dto.request;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNoteCreateRequest {

    @NotNull(message = "반려동물 ID는 필수입니다.")
    private Long petId;

    @NotNull(message = "기록 날짜는 필수입니다.")
    private LocalDate noteDate;

    private String content;
}
