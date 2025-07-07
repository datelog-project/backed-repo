package me.jinheum.datelog.controller;


import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.InviteRequest;
import me.jinheum.datelog.dto.ReconnectRequest;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.UserConnectionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("connections")
public class UserConnectionController {

    private final UserConnectionService connectionService;

    @PostMapping("/invite")
    public ResponseEntity<?> invite(@RequestBody InviteRequest request,
                                    @AuthenticationPrincipal UserAccount user) {
        connectionService.invitePartner(user.getId(), request.partnerEmail());
        return ResponseEntity.ok("초대를 보냈습니다.");
    }

    @PostMapping("/{connectionId}/accept")
    public ResponseEntity<?> acceptInvite(@PathVariable UUID connectionId,
                                        @AuthenticationPrincipal UserAccount user) {
        connectionService.acceptInvite(connectionId, user.getId());
        return ResponseEntity.ok("초대를 수락했습니다.");
    }

    @PostMapping("/{connectionId}/reject")
    public ResponseEntity<?> rejectInvite(@PathVariable UUID connectionId,
                                        @AuthenticationPrincipal UserAccount user) {
        connectionService.rejectInvite(connectionId, user.getId());
        return ResponseEntity.ok("초대를 거절했습니다.");
    }

    @PostMapping("/{connectionId}/end")
    public ResponseEntity<?> endConnection(@PathVariable UUID connectionId,
                                           @AuthenticationPrincipal UserAccount user) {
        connectionService.endConnection(connectionId, user.getId());
        return ResponseEntity.ok("연결이 종료되었습니다.");
    }

    @PostMapping("/reconnect")
    public ResponseEntity<?> reconnect(@RequestBody ReconnectRequest request,
                                       @AuthenticationPrincipal UserAccount user) {
        connectionService.reconnectByEmail(user.getId(), request.partnerEmail());
        return ResponseEntity.ok("재결합 되었습니다.");
    }
}