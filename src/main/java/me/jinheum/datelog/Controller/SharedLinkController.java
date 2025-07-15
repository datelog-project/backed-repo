package me.jinheum.datelog.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.ShareResponse;
import me.jinheum.datelog.dto.WithLogResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.SharedLinkService;

@RestController
@RequiredArgsConstructor
public class SharedLinkController {

    private final SharedLinkService sharedLinkService;
    
    @Operation(summary = "공유 할 URL 등록", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/with-logs/{withLogId}/share")
    public ResponseEntity<ShareResponse> createShareLink(
        @PathVariable UUID withLogId,
        @AuthenticationPrincipal UserAccount user) {

        UUID sharedLinkId = sharedLinkService.createSharedLink(withLogId, user.getId());
        String shareUrl = "http://localhost:8080/share/" + sharedLinkId.toString();

        return ResponseEntity.ok(new ShareResponse("공유 링크 생성됨 : ", shareUrl));
    }

    @Operation(summary = "공유 된 링크 열람", security = @SecurityRequirement(name = "bearerAuth" ))
    @GetMapping("/share/{sharedLinkId}")
    public ResponseEntity<WithLogResponse> getSharedLog(@PathVariable UUID sharedLinkId) {
        WithLogResponse response = sharedLinkService.getSharedLogView(sharedLinkId);
        return ResponseEntity.ok(response);
    }
}
