package com.newleaseonlife.SafeDogBe.domain.pet.dto.response;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.SpeciesType;
import java.time.LocalDateTime;

/**
 * 초대 링크 진입 시 반환하는 반려동물·초대자 정보.
 * FE는 이 정보를 바탕으로 "홍길동님이 {petName}의 공동 보호자로 초대했습니다." 화면을 구성.
 *
 * @param petId       반려동물 ID
 * @param petName     반려동물 이름
 * @param petSpecies  반려동물 종류 (선택)
 * @param inviterName 초대한 보호자 닉네임
 * @param expiredAt   초대 코드 만료 일시
 */
public record InviteInfoResponse(
        Long petId,
        String petName,
        SpeciesType petSpecies,
        String inviterName,
        LocalDateTime expiredAt
) {
}
