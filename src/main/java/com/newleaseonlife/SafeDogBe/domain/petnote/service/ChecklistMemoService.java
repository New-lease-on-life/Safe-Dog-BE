package com.newleaseonlife.SafeDogBe.domain.petnote.service;

import com.newleaseonlife.SafeDogBe.domain.care.entity.DailyChecklist;
import com.newleaseonlife.SafeDogBe.domain.care.repository.DailyChecklistRepository;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.PetGuardianRole;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetGuardianRepository;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.ChecklistMemoRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.ChecklistMemoResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.entity.ChecklistMemo;
import com.newleaseonlife.SafeDogBe.domain.petnote.repository.ChecklistMemoRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CareErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChecklistMemoService {

  private final ChecklistMemoRepository checklistMemoRepository;
  private final DailyChecklistRepository dailyChecklistRepository;
  private final UserRepository userRepository;
  private final PetGuardianRepository petGuardianRepository;

  /** 1. 메모 목록 조회 */
  public List<ChecklistMemoResponse> getMemos(Long checklistId) {
    return checklistMemoRepository.findAllByDailyChecklistIdWithAuthor(checklistId)
        .stream().map(ChecklistMemoResponse::from).toList();
  }

  /** 2. 메모 생성 */
  @Transactional
  public ChecklistMemoResponse createMemo(Long checklistId, Long userId, ChecklistMemoRequest request) {
    DailyChecklist checklist = getChecklistOrThrow(checklistId);
    User user = getUserOrThrow(userId);

    ChecklistMemo memo = ChecklistMemo.builder()
        .dailyChecklist(checklist)
        .author(user)
        .content(request.content())
        .build();

    return ChecklistMemoResponse.from(checklistMemoRepository.save(memo));
  }

  @Transactional
  public void createMemosBulk(Long checklistId, Long userId, List<String> contents) {
    DailyChecklist checklist = getChecklistOrThrow(checklistId);
    User user = getUserOrThrow(userId);

    // 데이터를 메모리에서 먼저 다 생성합니다.
    List<ChecklistMemo> memos = contents.stream()
        .map(content -> ChecklistMemo.builder()
            .dailyChecklist(checklist)
            .author(user)
            .content(content)
            .build())
        .toList();

    // 💡 JPA saveAll()을 사용하거나 JDBC Batch를 사용하여 한 번에 꽂아 넣습니다.
    checklistMemoRepository.saveAll(memos);
  }

  /** 3. 메모 수정 (작성자 본인만) */
  @Transactional
  public ChecklistMemoResponse updateMemo(Long memoId, Long userId, ChecklistMemoRequest request) {
    ChecklistMemo memo = getMemoOrThrow(memoId);

    // [권한 방어] 수정은 무조건 작성자 본인만 가능
    if (!memo.getAuthor().getId().equals(userId)) {
      throw new BusinessException(CommonErrorCode.NO_PERMISSION);
    }

    memo.updateContent(request.content());
    return ChecklistMemoResponse.from(memo);
  }

  /** 4. 메모 삭제 (작성자 또는 해당 동물의 OWNER) */
  @Transactional
  public void deleteMemo(Long memoId, Long userId) {
    ChecklistMemo memo = getMemoOrThrow(memoId);

    // 보안 로직: 클라이언트가 보내는 petId를 믿지 않고, DB의 메모가 바라보는 실제 반려동물 ID를 추출 (IDOR 방어)
    Long realPetId = memo.getDailyChecklist().getPet().getId();

    boolean isAuthor = memo.getAuthor().getId().equals(userId);
    boolean isOwner = petGuardianRepository.findByPetIdAndUserId(realPetId, userId)
        .map(guardian -> guardian.getRole() == PetGuardianRole.OWNER)
        .orElse(false);

    // [권한 방어] 작성자 본인 이거나 해당 동물의 관리자(OWNER)인 경우만 삭제 허용
    if (!isAuthor && !isOwner) {
      throw new BusinessException(CommonErrorCode.NO_PERMISSION);
    }

    checklistMemoRepository.delete(memo);
  }

  // --- Helpers ---
  private DailyChecklist getChecklistOrThrow(Long id) {
    return dailyChecklistRepository.findById(id)
        .orElseThrow(() -> new BusinessException(CareErrorCode.CHECKLIST_NOT_FOUND));
  }

  private User getUserOrThrow(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
  }

  private ChecklistMemo getMemoOrThrow(Long id) {
    // 커스텀 에러 코드가 없다면 임시로 CareErrorCode 추가 필요
    return checklistMemoRepository.findById(id)
        .orElseThrow(() -> new BusinessException(CommonErrorCode.BAD_REQUEST));
  }
}