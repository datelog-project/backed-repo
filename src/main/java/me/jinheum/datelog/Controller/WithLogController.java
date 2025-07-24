package me.jinheum.datelog.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.ApiResponse;
import me.jinheum.datelog.dto.WithLogPreviewResponse;
import me.jinheum.datelog.dto.WithLogRequest;
import me.jinheum.datelog.dto.WithLogResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.WithLogService;

@RestController
@RequestMapping("/with-logs")
@RequiredArgsConstructor
public class WithLogController {

    private final WithLogService withLogService;

    @Operation(summary = "게시글 등록하기", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{connectionId}")
    public ResponseEntity<ApiResponse> createWithLog(@AuthenticationPrincipal UserAccount user,
                                                    @PathVariable UUID connectionId, 
                                                    @RequestBody @Valid WithLogRequest request) {

        withLogService.createWithLog(connectionId, request, user.getId());
        return ResponseEntity.ok(new ApiResponse("게시글이 등록되었습니다."));
    }

    @Operation(summary = "모든 게시글 확인하기", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{connectionId}")
    public ResponseEntity<List<WithLogPreviewResponse>> getWithLogs(
            @PathVariable UUID connectionId,
            @AuthenticationPrincipal UserAccount user
    ) {
        List<WithLogPreviewResponse> previews = withLogService.getWithLogPreviews(connectionId, user.getId());
        return ResponseEntity.ok(previews);
    }

    @Operation(summary = "게시글 상세보기", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{withLogId}/details")
    public ResponseEntity<WithLogResponse> getWithLogDetail(
            @PathVariable UUID withLogId,
            @AuthenticationPrincipal UserAccount user) {
        
        WithLogResponse response = withLogService.getWithLogDetail(withLogId, user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 수정하기", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping(value = "/{withLogId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WithLogResponse> updateWithLog(
            @PathVariable UUID withLogId,
            @RequestPart("request") WithLogRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserAccount user) {

        WithLogResponse updated = withLogService.updateWithLog(withLogId, request, images, user.getId());
        return ResponseEntity.ok(updated);
    }


    @Operation(summary = "게시글 삭제하기", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{withLogId}")
    public ResponseEntity<ApiResponse> deleteWithLog(
            @PathVariable UUID withLogId,
            @AuthenticationPrincipal UserAccount user) {

        withLogService.deleteWithLog(withLogId, user.getId());

        return ResponseEntity.ok(new ApiResponse("게시글이 삭제되었습니다."));
    }
}