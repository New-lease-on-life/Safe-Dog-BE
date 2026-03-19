// domain/petnote/controller/PetNoteController.java
package com.newleaseonlife.SafeDogBe.domain.petnote.controller;

import com.newleaseonlife.SafeDogBe.domain.pet.service.PetService;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteCreateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.request.PetNoteUpdateRequest;
import com.newleaseonlife.SafeDogBe.domain.petnote.dto.response.PetNoteResponse;
import com.newleaseonlife.SafeDogBe.domain.petnote.service.PetNoteService;
import com.newleaseonlife.SafeDogBe.global.error.BusinessException;
import com.newleaseonlife.SafeDogBe.global.error.domain.CommonErrorCode;
import com.newleaseonlife.SafeDogBe.global.security.CustomPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "PetNote", description = "반려노트(기록장) CRUD 및 날짜별 조회 API")
@Slf4j
@Validated
@RestController
@RequestMapping("/api/pet-notes")
@RequiredArgsConstructor
public class PetNoteController {

  private final PetNoteService petNoteService;
  private final PetService petService;

  @Operation(
      summary = "특정 반려동물의 전체 반려노트 조회",
      description = "해당 반려동물의 모든 반려노트 목록을 최신순으로 조회합니다. 데이터가 없을 경우 안내 메시지가 포함된 객체를 반환합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(mediaType = "application/json", examples = {
              @ExampleObject(name = "데이터가 있는 경우", value = "[{\"id\": 1, \"content\": \"오늘 병원 다녀옴\"}]"),
              @ExampleObject(name = "데이터가 없는 경우", value = "{\"message\": \"등록된 반려노트가 없습니다.\", \"petId\": 1, \"notes\": []}")
          })
      ),
      @ApiResponse(responseCode = "403", description = "해당 반려동물에 대한 접근 권한(소유권)이 없음", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 반려동물 ID", content = @Content)
  })
  @GetMapping
  public ResponseEntity<Object> getNotesByPetId(
      @Parameter(description = "조회할 대상 반려동물의 ID", example = "1")
      @RequestParam Long petId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    log.info("[PetNoteController] getNotesByPetId petId={}, userId={}", petId,
        principal.getUser().getId());
    List<PetNoteResponse> response = petNoteService.findByPetId(petId, principal.getUser().getId());

    if (response == null || response.isEmpty()) {
      Map<String, Object> emptyResponse = new HashMap<>();
      emptyResponse.put("message", "등록된 반려노트가 없습니다.");
      emptyResponse.put("petId", petId);
      emptyResponse.put("notes", response);
      return ResponseEntity.ok(emptyResponse);
    }

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "특정 날짜의 반려노트 조회",
      description = "달력 등에서 특정 날짜를 선택했을 때 해당 일자의 반려노트 목록을 조회합니다. 데이터가 없을 경우 안내 메시지가 포함된 객체를 반환합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(mediaType = "application/json", examples = {
              @ExampleObject(name = "데이터가 있는 경우", value = "[{\"id\": 1, \"content\": \"오늘 병원 다녀옴\"}]"),
              @ExampleObject(name = "데이터가 없는 경우", value = "{\"message\": \"등록된 반려노트가 없습니다.\", \"petId\": 1, \"date\": \"2026-03-19\", \"notes\": []}")
          })
      ),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content)
  })
  @GetMapping("/by-date")
  public ResponseEntity<Object> getNotesByPetIdAndDate(
      @Parameter(description = "조회할 반려동물의 ID", example = "1")
      @RequestParam Long petId,
      @Parameter(description = "조회할 날짜 (yyyy-MM-dd)", example = "2026-03-19")
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate noteDate,
      @AuthenticationPrincipal CustomPrincipal principal) {
    log.info("[PetNoteController] getNotesByPetIdAndDate petId={}, noteDate={}, userId={}",
        petId, noteDate, principal.getUser().getId());

    List<PetNoteResponse> response = petNoteService.findByPetIdAndDate(
        petId, noteDate, principal.getUser().getId());

    if (response == null || response.isEmpty()) {
      Map<String, Object> emptyResponse = new HashMap<>();
      emptyResponse.put("message", "등록된 반려노트가 없습니다.");
      emptyResponse.put("petId", petId);
      emptyResponse.put("date", noteDate.toString());
      emptyResponse.put("notes", response);
      return ResponseEntity.ok(emptyResponse);
    }

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "반려노트 단건 상세 조회",
      description = "노트 ID(PK)를 통해 특정 반려노트 1개의 상세 정보를 조회합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = PetNoteResponse.class))),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 반려노트 ID", content = @Content)
  })
  @GetMapping("/{noteId}")
  public ResponseEntity<PetNoteResponse> getNote(
      @Parameter(description = "조회할 반려노트 ID", example = "10")
      @PathVariable Long noteId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    PetNoteResponse response = petNoteService.findById(noteId, principal.getUser().getId());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "반려노트 생성(등록)",
      description = "새로운 반려노트를 작성합니다. 요청하는 유저가 해당 반려동물의 보호자(소유권)여야만 등록이 가능합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(schema = @Schema(implementation = PetNoteResponse.class))),
      @ApiResponse(responseCode = "400", description = "필수 파라미터(petId, noteDate) 누락", content = @Content),
      @ApiResponse(responseCode = "403", description = "반려동물에 대한 접근 권한 없음", content = @Content)
  })
  @PostMapping
  public ResponseEntity<PetNoteResponse> createNote(
      @Valid @RequestBody PetNoteCreateRequest request,
      @AuthenticationPrincipal CustomPrincipal principal) {
    PetNoteResponse response = petNoteService.create(request, principal.getUser().getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Operation(
      summary = "반려노트 부분 수정",
      description = "등록된 반려노트의 내용(content)이나 기록일(noteDate)을 수정합니다. 변경을 원하지 않는 필드는 null로 보내면 기존 값이 유지됩니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = PetNoteResponse.class))),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 반려노트 ID", content = @Content)
  })
  @PatchMapping("/{noteId}")
  public ResponseEntity<PetNoteResponse> updateNote(
      @Parameter(description = "수정할 반려노트 ID", example = "10")
      @PathVariable Long noteId,
      @Valid @RequestBody PetNoteUpdateRequest request,
      @AuthenticationPrincipal CustomPrincipal principal) {
    PetNoteResponse response = petNoteService.update(noteId, request, principal.getUser().getId());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "반려노트 삭제",
      description = "등록된 반려노트를 삭제합니다. 해당 반려동물의 보호자만 삭제가 가능합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공 (반환 데이터 없음)", content = @Content),
      @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content),
      @ApiResponse(responseCode = "404", description = "존재하지 않는 반려노트 ID", content = @Content)
  })
  @DeleteMapping("/{noteId}")
  public ResponseEntity<Void> deleteNote(
      @Parameter(description = "삭제할 반려노트 ID", example = "10")
      @PathVariable Long noteId,
      @AuthenticationPrincipal CustomPrincipal principal) {
    petNoteService.delete(noteId, principal.getUser().getId());
    return ResponseEntity.noContent().build();
  }
}