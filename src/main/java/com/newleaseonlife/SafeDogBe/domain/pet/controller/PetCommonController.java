package com.newleaseonlife.SafeDogBe.domain.pet.controller;

import com.newleaseonlife.SafeDogBe.domain.pet.dto.response.BreedResponse;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.CatBreed;
import com.newleaseonlife.SafeDogBe.domain.pet.entity.enums.DogBreed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "반려동물 품목 조회 엔드포인트(개발완)", description = "반려동물 공통 API")
public class PetCommonController {

  @Operation(summary = "강아지 품종 목록 조회", description = "바텀 시트 렌더링용 강아지 품종 목록을 반환합니다.")
  @GetMapping("/breeds/dogs")
  public ResponseEntity<List<BreedResponse>> getDogBreeds() {
    List<BreedResponse> response = Arrays.stream(DogBreed.values())
        .map(breed -> new BreedResponse(breed.name(), breed.getDescription()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "고양이 품종 목록 조회", description = "바텀 시트 렌더링용 고양이 품종 목록을 반환합니다.")
  @GetMapping("/breeds/cats")
  public ResponseEntity<List<BreedResponse>> getCatBreeds() {
    List<BreedResponse> response = Arrays.stream(CatBreed.values())
        .map(breed -> new BreedResponse(breed.name(), breed.getDescription()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }
}
