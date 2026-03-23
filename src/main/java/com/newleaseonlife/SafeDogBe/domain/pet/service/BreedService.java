package com.newleaseonlife.SafeDogBe.domain.pet.service;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.BreedResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.CatBreed;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.DogBreed;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BreedService {

  /**
   * 강아지 품종 목록 반환
   */
  public List<BreedResponse> getDogBreeds() {
    return Arrays.stream(DogBreed.values())
        .map(breed -> new BreedResponse(breed.name(), breed.getDescription()))
        .collect(Collectors.toList());
  }

  /**
   * 고양이 품종 목록 반환
   */
  public List<BreedResponse> getCatBreeds() {
    return Arrays.stream(CatBreed.values())
        .map(breed -> new BreedResponse(breed.name(), breed.getDescription()))
        .collect(Collectors.toList());
  }
}