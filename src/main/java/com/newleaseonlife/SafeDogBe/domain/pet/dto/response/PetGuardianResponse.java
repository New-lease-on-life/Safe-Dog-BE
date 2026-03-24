package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetGuardianResponse {
    private Long id;
    private Long userId;
    private String nickname; // 추가: 보호자 닉네임 노출용
    private PetGuardianRole role;
    private boolean isUserDeleted; // 추가: 탈퇴 회원 여부 확인용
}