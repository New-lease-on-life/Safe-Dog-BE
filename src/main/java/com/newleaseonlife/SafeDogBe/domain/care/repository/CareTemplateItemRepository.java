package com.newleaseonlife.SafeDogBe.domain.care.repository;

import com.newleaseonlife.SafeDogBe.domain.care.entity.CareTemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/** 3월 18일 수정*/
public interface CareTemplateItemRepository extends JpaRepository<CareTemplateItem, Long> {

  List<CareTemplateItem> findByCareTemplateIdOrderBySortOrderAsc(Long careTemplateId);

  List<CareTemplateItem> findByCareTemplateIdIn(List<Long> careTemplateIds);

  /**
   * ✅ 수정: @Modifying 추가 필수.
   * JPQL 벌크 DELETE는 영속성 컨텍스트를 우회하므로
   * Service에서 호출 후 EntityManager.flush()+clear() 또는
   * @Modifying(clearAutomatically = true) 옵션 사용.
   */
  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM CareTemplateItem c WHERE c.careTemplate.id = :careTemplateId")
  void deleteByCareTemplateId(Long careTemplateId);
}