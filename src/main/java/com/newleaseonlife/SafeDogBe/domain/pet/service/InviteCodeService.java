package com.newleaseonlife.SafeDogBe.domain.pet.service;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.InviteCodeResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.InviteInfoResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetGuardianResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.InviteCode;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.PetGuardian;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.converter.PetConverter;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.InviteCodeRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 공동 보호자 초대 코드 서비스.
 * 코드 생성(OWNER만 가능), 초대 정보 조회(비인증), 코드 기반 보호자 참여 처리.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteCodeService {

    /** 초대 코드 유효 기간(일) */
    private static final int INVITE_EXPIRE_DAYS = 7;

    private final InviteCodeRepository inviteCodeRepository;
    private final PetRepository petRepository;
    private final PetGuardianRepository petGuardianRepository;
    private final UserRepository userRepository;
    private final PetConverter petConverter;

    /**
     * 초대 코드 생성. 반려동물 OWNER만 호출 가능.
     * 기존에 유효한 코드가 있어도 새로 생성(매 요청마다 새 코드 발급).
     */
    @Transactional
    public InviteCodeResponse generateInviteCode(Long petId, Long ownerUserId) {
        log.info("[InviteCodeService] generateInviteCode petId={}, ownerUserId={}", petId, ownerUserId);

        Pet pet = petRepository.findByIdAndUserId(petId, ownerUserId)
                .orElseThrow(() -> {
                    if (petRepository.findById(petId).isEmpty()) {
                        return new BusinessException(PetErrorCode.PET_NOT_FOUND);
                    }
                    return new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
                });

        User inviter = userRepository.findById(ownerUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // UUID 기반 8자리 대문자 코드 생성 (충돌 가능성 극히 낮음)
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(INVITE_EXPIRE_DAYS);

        InviteCode inviteCode = InviteCode.builder()
                .code(code)
                .pet(pet)
                .inviter(inviter)
                .expiredAt(expiredAt)
                .build();
        inviteCodeRepository.save(inviteCode);

        log.info("[InviteCodeService] 초대 코드 생성 완료 code={}, expiredAt={}", code, expiredAt);
        return new InviteCodeResponse(code, expiredAt);
    }

    /**
     * 초대 코드 기반 반려동물·초대자 정보 조회. 비인증 API.
     * FE 초대 링크 진입 화면 구성에 사용.
     */
    public InviteInfoResponse getInviteInfo(String code) {
        log.info("[InviteCodeService] getInviteInfo code={}", code);
        InviteCode inviteCode = findValidCode(code);
        Pet pet = inviteCode.getPet();
        return new InviteInfoResponse(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                inviteCode.getInviter().getNickname(),
                inviteCode.getExpiredAt()
        );
    }

    /**
     * 초대 코드로 공동 보호자(CAREGIVER) 등록.
     * 코드 유효성 검증 → 이미 보호자이면 409 → PetGuardian 생성 → 코드 사용 처리.
     */
    @Transactional
    public PetGuardianResponse joinByInviteCode(String code, Long joinUserId) {
        log.info("[InviteCodeService] joinByInviteCode code={}, userId={}", code, joinUserId);

        InviteCode inviteCode = findValidCode(code);
        Pet pet = inviteCode.getPet();

        User joinUser = userRepository.findById(joinUserId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (petGuardianRepository.existsByPetIdAndUserId(pet.getId(), joinUserId)) {
            log.warn("[InviteCodeService] joinByInviteCode 실패 - 이미 보호자 petId={}, userId={}", pet.getId(), joinUserId);
            throw new BusinessException(PetErrorCode.PET_GUARDIAN_ALREADY_EXISTS);
        }

        PetGuardian guardian = PetGuardian.builder()
                .user(joinUser)
                .pet(pet)
                .role(PetGuardianRole.CAREGIVER)
                .build();
        PetGuardian saved = petGuardianRepository.save(guardian);
        inviteCode.use();

        log.info("[InviteCodeService] 보호자 등록 완료 guardianId={}, petId={}", saved.getId(), pet.getId());
        return petConverter.toGuardianResponse(saved);
    }

    /** 코드로 유효한 InviteCode 조회. 없거나 만료/사용됐으면 예외 */
    private InviteCode findValidCode(String code) {
        InviteCode inviteCode = inviteCodeRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException(PetErrorCode.INVITE_CODE_NOT_FOUND));
        if (inviteCode.isExpiredOrUsed(LocalDateTime.now())) {
            throw new BusinessException(PetErrorCode.INVITE_CODE_EXPIRED_OR_USED);
        }
        return inviteCode;
    }
}
