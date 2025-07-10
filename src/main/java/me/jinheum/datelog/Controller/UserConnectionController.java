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
import me.jinheum.datelog.dto.ApiResponse;
import me.jinheum.datelog.dto.InviteRequest;
import me.jinheum.datelog.dto.InviteResponse;
import me.jinheum.datelog.dto.ReconnectRequest;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.UserAccountService;
import me.jinheum.datelog.service.UserConnectionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("connections")
public class UserConnectionController {

    private final UserConnectionService connectionService;
    private final UserAccountService userAccountService;

    @PostMapping("/invite")
    public ResponseEntity<InviteResponse> invite(@RequestBody InviteRequest request,
                                            @AuthenticationPrincipal UserAccount user) {
        UserAccount partner = userAccountService.getUserByEmail(request.partnerEmail());
        UUID connectionId = connectionService.invitePartner(user, partner);
        return ResponseEntity.ok(new InviteResponse("초대를 보냈습니다.", connectionId));
    }

    @PostMapping("/{connectionId}/accept")
    public ResponseEntity<ApiResponse> acceptInvite(@PathVariable UUID connectionId,
                                        @AuthenticationPrincipal UserAccount user) {
        connectionService.acceptInvite(connectionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("초대를 수락했습니다."));
    } //ok

    @PostMapping("/{connectionId}/reject")
    public ResponseEntity<ApiResponse> rejectInvite(@PathVariable UUID connectionId,
                                        @AuthenticationPrincipal UserAccount user) {
        connectionService.rejectInvite(connectionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("초대를 거절했습니다."));
    } //ok

    @PostMapping("/{connectionId}/end")
    public ResponseEntity<ApiResponse> endConnection(@PathVariable UUID connectionId,
                                           @AuthenticationPrincipal UserAccount user) {
        connectionService.endConnection(connectionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("연결이 종료되었습니다."));
    } //ok

    @PostMapping("/reconnect")
    public ResponseEntity<ApiResponse> reconnect(@RequestBody ReconnectRequest request,
                                       @AuthenticationPrincipal UserAccount user) {
        connectionService.reconnectByEmail(user.getId(), request.partnerEmail());
        return ResponseEntity.ok(new ApiResponse("재결합 되었습니다."));
    } //ok
}