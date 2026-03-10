package com.newleaseonlife.SafeDogBe.domain.auth.repository;

import com.newleaseonlife.SafeDogBe.domain.auth.entity.UserDevice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 기기 단위 로그인 정보 영속화 */
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    /** deviceId로 기기 로그인 정보 조회 */
    Optional<UserDevice> findByDeviceId(String deviceId);
}
