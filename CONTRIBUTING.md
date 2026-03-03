# 🤝 Contributing to Safe-Dog-BE

New lease on life 팀의 프로젝트에 참여해주셔서 감사합니다! 일관된 협업을 위해 아래 규칙을 준수해주세요.

## 🌿 브랜치 전략 (All Lowercase)
- **main**: 배포 가능한 안정 버전
- **develop**: 다음 배포를 위한 통합 브랜치 (기본 작업 브랜치)
- **feat/***: 새로운 기능 개발 (ex: `feat/users`)
- **hotfix/***: 배포 버전의 긴급 수정

## 🔄 작업 흐름
1. **이슈 생성**: 작업 전 이슈를 생성하고 본인을 담당자(Assignee)로 지정합니다.
2. **브랜치 생성**: `develop`에서 최신 내용을 `pull` 받은 후, `feat/도메인명` 브랜치를 생성합니다.
3. **작업 및 커밋**: 커밋 메시지 규칙을 준수하며 작업합니다.
4. **Pull Request**: `develop` 브랜치로 PR을 보냅니다.
5. **코드 리뷰**: **최소 1명 이상의 승인(Approve)** 이 필수입니다.
6. **머지**: 리뷰 반영 후 **Squash and Merge** 방식으로 머지합니다.
7. **병합 이후:** 브랜치는 삭제 해주세요.

## 💬 커밋 메시지 규칙
- **형식**: `[type]: description` (예: `[feat]: add user signup endpoint)`
- **종류**:
  - `feat`: 새로운 기능 추가
  - `fix`: 버그 수정
  - `refactor`: 코드 리팩토링
  - `docs`: 문서 수정 (README 등)
  - `test`: 테스트 코드 추가 및 수정
  - `chore`: 빌드 업무, 패키지 매니저 설정 등 기타 작업

## 🛠️ 리팩토링 원칙 (Refactoring Guide)

- **DRY (Don't Repeat Yourself)**: 중복되는 로직은 별도 메서드나 클래스로 추출합니다.
- **가독성 우선**: 메서드 길이는 가급적 20줄 이내로 유지하며, 한 메서드는 한 가지 기능만 수행합니다.
- **Early Return**: `if-else` 중첩을 피하기 위해 조건이 맞지 않으면 빠르게 리턴합니다.
- **네이밍**: 변수나 메서드명만 보고도 로직을 이해할 수 있도록 명확하게 명명합니다.

## 💻 코드 스타일 & 규칙

### 1. 기본 원칙
- **Lombok** 라이브러리를 적극 활용합니다.
- 메서드명은 **동사**로 시작하며, 변수명은 **camelCase**를 사용합니다.
- 패키지별로 클래스를 관리합니다

### 2. DTO 구조 가이드라인
모든 DTO는 역할에 따라 아래 어노테이션을 필수로 포함합니다.

#### **요청(Request) 클래스**

```Java
@Getter
@NoArgsConstructor  // JSON 역직렬화(Request Body 매핑) 필수
@AllArgsConstructor // 테스트 편의 및 빠른 객체 생성
@Builder            // 가독성 있는 객체 생성 및 부분 필드 테스트
public class UserCreateRequest { ... }
```

#### **응답(Response) 클래스**

```Java
@Getter
@Builder            // 서비스 계층에서 안전하고 가독성 있게 생성
@NoArgsConstructor  // 직렬화 대비 (Jackson 라이브러리 대응)
public class UserResponse { ... }
```

## 예외 처리

### 에러코드(ErrorCode.class)
ErrorCode.class에 각 도메인 영역에 맞추어서 에러 코드 추가하기.
1. code 넘버는 각 도메인 앞글자 + 001 ~ 으로 시작하기.
2. 코드 메세지는 기획팀이랑 상의하기(또는 일단 자세하게 작성하고 추후에 보고)


### 커스텀 예외 사용법(CustomException.class)
사용하는 시나리오

기획서의 "이미 가입된 계정이 있어요" 요구사항을 구현하는 회원가입 서비스 로직(MemberService)을 작성한다고 가정.

``` Java
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void signUp(SignUpRequestDto request) {
        // 1. 이름과 전화번호로 기존 회원 조회
        boolean isExist = memberRepository.existsByNameAndPhoneNumber(
                request.getName(), 
                request.getPhoneNumber()
        );

        // 2. 이미 존재하는 회원이면 예외 발생! (여기서 CustomException 사용)
        if (isExist) {
            throw new CustomException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        // 3. 예외가 안 터졌다면 정상 가입 로직 진행
        // memberRepository.save(...);
    }
}
```
이렇게 사용하면 자동으로 GlobalExceptionHandler에서 예외를 낚아 채서 아래와 같이 응답으로 보낼것임.
```json
{
  "status": 413,
  "code": "C002",
  "message": "10MB 이하의 이미지만 등록 가능합니다."
}
```

