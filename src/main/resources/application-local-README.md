# application-local.yaml 에 추가한 내용

## 1. Spring DataSource (H2)
<!-- 민감: password는 로컬 H2 기본값(빈 값) 사용. 운영/공유 DB 사용 시 환경 변수로 설정할 것. -->
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:safedog;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password:          # 로컬 H2는 빈 값 가능. 외부 DB면 환경 변수 사용
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
```

## 2. JPA
```yaml
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
    open-in-view: false
```

## 3. Redis
<!-- Redis 비밀번호 사용 시 spring.data.redis.password 는 환경 변수로만 설정하고 문서에는 적지 말 것. -->
```yaml
  data:
    redis:
      host: localhost
      port: 6379
```

## 4. OAuth2 Client (Google, Naver, Kakao)
<!-- 민감: client-id, client-secret 은 절대 실제 값으로 커밋하지 말 것. 환경 변수(OAUTH2_*_CLIENT_ID, OAUTH2_*_CLIENT_SECRET)로만 설정. -->
```yaml
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${OAUTH2_GOOGLE_CLIENT_ID:***}
            client-secret: ${OAUTH2_GOOGLE_CLIENT_SECRET:***}
            scope: email, profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            client-name: Google
            authorization-grant-type: authorization_code
          naver:
            client-id: ${OAUTH2_NAVER_CLIENT_ID:***}
            client-secret: ${OAUTH2_NAVER_CLIENT_SECRET:***}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            # birthyear, birthday: 만 14세 미만 가입 차단 검증용. 네이버 개발자 센터에서 해당 항목 동의 활성화 필요
            scope: name, email, birthyear, birthday
            client-name: Naver
          kakao:
            client-id: ${OAUTH2_KAKAO_CLIENT_ID:***}
            client-secret: ${OAUTH2_KAKAO_CLIENT_SECRET:***}
            client-authentication-method: client_secret_post
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            # account_birthyear, account_birthday: 만 14세 미만 가입 차단 검증용. 카카오 앱 설정에서 해당 항목 동의 활성화 필요
            scope: profile_nickname, account_email, account_birthyear, account_birthday
            client-name: Kakao
        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

## 5. 앱 OAuth2 리다이렉트 URL
```yaml
app:
  oauth2:
    redirect-uri-after-login: ${OAUTH2_REDIRECT_URI_AFTER_LOGIN:http://localhost:3000/oauth2/redirect}
```

## 6. JWT
<!-- 민감: accsecret, refsecret 은 절대 실제 키를 문서/커밋에 넣지 말 것. 환경 변수 JWT_ACCSECRET, JWT_REFSECRET 으로만 설정. 최소 32자 이상 권장. -->
```yaml
jwt:
  accsecret: ${JWT_ACCSECRET:***}   # 환경 변수로 설정. 실제 값 노출 금지
  refsecret: ${JWT_REFSECRET:***}  # 환경 변수로 설정. 실제 값 노출 금지
  access-token-expiration: 1800000
  refresh-token-expiration: 604800000
```

## 7. Logging
```yaml
logging:
  level:
    root: info
    org.hibernate.SQL: debug
```
