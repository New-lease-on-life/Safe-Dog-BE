package com.newleaseonlife.SafeDogBe.domain.petnote.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNoteUpdateRequest {

    private String content;
}
