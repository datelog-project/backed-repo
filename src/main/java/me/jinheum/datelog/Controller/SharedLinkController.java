package me.jinheum.datelog.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.ShareResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.SharedLinkService;

@RestController
@RequiredArgsConstructor
public class SharedLinkController {

    private final SharedLinkService sharedLinkService;
    
    @PostMapping("/with-logs/{withLogId}/share")
    public ResponseEntity<ShareResponse> createShareLink(
        @PathVariable UUID withLogId,
        @AuthenticationPrincipal UserAccount user) {

        UUID sharedLinkId = sharedLinkService.createSharedLink(withLogId, user.getId());
        String shareUrl = "http://localhost:8080/share/" + sharedLinkId.toString();

        return ResponseEntity.ok(new ShareResponse("공유 링크 생성됨 : ", shareUrl));
    }
}
