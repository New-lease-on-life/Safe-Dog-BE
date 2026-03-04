package com.newleaseonlife.SafeDogBe.domain.pet.repository;

import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Pet> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
