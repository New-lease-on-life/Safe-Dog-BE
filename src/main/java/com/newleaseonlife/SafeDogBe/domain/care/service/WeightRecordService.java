package com.newleaseonlife.SafeDogBe.domain.care.service;

import com.newleaseonlife.SafeDogBe.domain.care.converter.WeightRecordConverter;
import com.newleaseonlife.SafeDogBe.domain.care.dto.request.WeightRecordRequest;
import com.newleaseonlife.SafeDogBe.domain.care.dto.response.WeightRecordResponse;
import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyWeightRecord;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyWeightRecordRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 3월 18일 수정
 * 체중 기록 서비스.
 * 기획서 3: 보호자가 체중 수치(kg)를 직접 입력해서 저장.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeightRecordService {

  private final DailyWeightRecordRepository weightRecordRepository;
  private final PetRepository petRepository;
  private final UserRepository userRepository;
  private final WeightRecordConverter converter;

  /** 체중 기록 등록 또는 수정 */
  @Transactional
  public WeightRecordResponse saveWeightRecord(Long userId, WeightRecordRequest req) {
    Pet pet = petRepository.findById(req.getPetId())
        .orElseThrow(() -> new BusinessException(PetErrorCode.PET_NOT_FOUND));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

    // 당일 기록이 있으면 수정, 없으면 신규
    DailyWeightRecord record = weightRecordRepository
        .findByPetIdAndRecordDate(req.getPetId(), req.getRecordDate())
        .orElseGet(() -> DailyWeightRecord.builder()
            .pet(pet)
            .recordDate(req.getRecordDate())
            .build());

    record.updateWeight(req.getWeight(), user);
    weightRecordRepository.save(record);
    return converter.toResponse(record);
  }

  /** 기간 내 체중 이력 조회 */
  public List<WeightRecordResponse> getWeightHistory(Long petId, LocalDate from, LocalDate to) {
    return converter.toResponseList(
        weightRecordRepository.findByPetIdAndRecordDateBetweenOrderByRecordDateDesc(petId, from, to));
  }
}