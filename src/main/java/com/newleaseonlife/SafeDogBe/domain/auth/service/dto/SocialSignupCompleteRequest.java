package com.newleaseonlife.SafeDogBe.domain.auth.service.dto;

import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SocialSignupCompleteRequest(
    @NotEmpty(message = "약관 동의 목록은 필수입니다") @Valid List<TermAgreementRequest> terms,
    String inviteCode // 초대 코드는 선택(Nullable)
) {}
