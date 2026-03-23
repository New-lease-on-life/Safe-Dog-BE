package com.newleaseonlife.SafeDogBe.global.error.domain;

import com.newleaseonlife.SafeDogBe.global.error.ApiCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 홈 화면 API 전용 비즈니스 오류 코드
 * 수정 사유: AuthErrorCode 등 기존 도메인 에러 코드와의 통일성을 위해 3자리 코드로 규격을 맞춤.
 */
@AllArgsConstructor
@Getter
public enum HomeErrorCode implements ApiCode {

    // --- 400 BAD REQUEST ---
    /** 잘못된 날짜 형식 또는 필수 파라미터 누락 */
    HOME_INVALID_REQUEST(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 요청이거나 필수 파라미터가 누락되었습니다."),
    HOME_INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, 400, "유효하지 않은 날짜 형식입니다."),

    // --- 403 FORBIDDEN ---
    /** 반려동물 정보 또는 메모에 대한 접근 권한 없음 */
    HOME_PET_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "해당 반려동물의 정보를 조회할 권한이 없습니다."),
    HOME_NOTE_ACCESS_DENIED(HttpStatus.FORBIDDEN, 403, "메모에 대한 접근 권한이 없습니다."),

    // --- 404 NOT FOUND ---
    /** 회원 정보, 반려동물 목록, 특정 반려동물 데이터 미존재 */
    HOME_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "회원 정보를 찾을 수 없습니다."),
    HOME_PET_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "등록된 반려동물이 존재하지 않습니다."),
    HOME_SPECIFIC_PET_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "요청하신 반려동물 정보를 찾을 수 없습니다."),
    HOME_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, 404, "조회하려는 홈 데이터를 찾을 수 없습니다."),

    // --- 500 INTERNAL SERVER ERROR ---
    /** 서버 내부 계산 로직 오류 */
    HOME_CALCULATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 500, "홈 데이터 처리 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final Integer code;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}