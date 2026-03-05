package com.newleaseonlife.SafeDogBe.domain.pet.service;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.request.PetUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.PetResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.Pet;
import com.newleaseonlife.SafeDogBe.domain.pet.converter.PetConverter;
import com.newleaseonlife.SafeDogBe.domain.pet.repository.PetRepository;
import com.newleaseonlife.SafeDogBe.domain.user.entity.User;
import com.newleaseonlife.SafeDogBe.domain.user.repository.UserRepository;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.PetErrorCode;
import com.newleaseonlife.SafeDogBe.global.error.domain.UserErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetConverter petConverter;

    public List<PetResponse> findMyPets(Long userId) {
        log.debug("[PetService] findMyPets userId={}", userId);
        List<Pet> pets = petRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return petConverter.toResponseList(pets);
    }

    public PetResponse findById(Long petId, Long userId) {
        log.debug("[PetService] findById petId={}, userId={}", petId, userId);
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> {
                    if (petRepository.findById(petId).isEmpty()) {
                        return new BusinessException(PetErrorCode.PET_NOT_FOUND);
                    }
                    return new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
                });
        return petConverter.toResponse(pet);
    }

    @Transactional
    public PetResponse create(Long userId, PetCreateRequest request) {
        log.info("[PetService] create userId={}, name={}", userId, request.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        boolean isNeutered = request.getIsNeutered() != null && request.getIsNeutered();
        Pet pet = Pet.builder()
                .user(user)
                .name(request.getName())
                .species(request.getSpecies())
                .breed(request.getBreed())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .isNeutered(isNeutered)
                .profileImageUrl(request.getProfileImageUrl())
                .build();
        petRepository.save(pet);
        log.info("[PetService] create 완료 petId={}", pet.getId());
        return petConverter.toResponse(pet);
    }

    @Transactional
    public PetResponse update(Long petId, Long userId, PetUpdateRequest request) {
        log.info("[PetService] update petId={}, userId={}", petId, userId);
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> {
                    if (petRepository.findById(petId).isEmpty()) {
                        return new BusinessException(PetErrorCode.PET_NOT_FOUND);
                    }
                    return new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
                });

        pet.update(
                request.getName(),
                request.getSpecies(),
                request.getBreed(),
                request.getBirthDate(),
                request.getGender(),
                request.getIsNeutered(),
                request.getProfileImageUrl()
        );
        log.info("[PetService] update 완료 petId={}", petId);
        return petConverter.toResponse(pet);
    }

    @Transactional
    public void delete(Long petId, Long userId) {
        log.info("[PetService] delete petId={}, userId={}", petId, userId);
        if (!petRepository.existsByIdAndUserId(petId, userId)) {
            if (petRepository.findById(petId).isEmpty()) {
                throw new BusinessException(PetErrorCode.PET_NOT_FOUND);
            }
            throw new BusinessException(PetErrorCode.PET_ACCESS_DENIED);
        }
        petRepository.deleteById(petId);
        log.info("[PetService] delete 완료 petId={}", petId);
    }
}
