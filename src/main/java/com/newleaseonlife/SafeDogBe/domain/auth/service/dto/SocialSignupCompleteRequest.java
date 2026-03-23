package com.newleaseonlife.SafeDogBe.domain.auth.service.dto;

import com.newleaseonlife.SafeDogBe.domain.term.dto.request.TermAgreementRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record SocialSignupCompleteRequest(
    @NotEmpty(message = "약관 동의 목록은 필수입니다.")
    @Valid List<TermAgreementRequest> terms,

    String inviteCode, // 선택

    @Past(message = "생년월일은 과거의 날짜여야 합니다.") // ✅ 추가: 미래 날짜 입력 원천 차단
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate birthDate // 선택 (OAuth에서 못 받은 경우 여기서 입력받음)

) {

}
