package me.jinheum.datelog.controller;


import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.ApiResponse;
import me.jinheum.datelog.dto.InviteRequest;
import me.jinheum.datelog.dto.InviteResponse;
import me.jinheum.datelog.dto.ReconnectCheckResponse;
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

    @Operation(summary = "초대 보내기", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/invite")
    public ResponseEntity<InviteResponse> invite(@RequestBody InviteRequest request,
                                            @AuthenticationPrincipal UserAccount user) {
        UserAccount partner = userAccountService.getUserByEmail(request.partnerEmail());
        UUID connectionId = connectionService.invitePartner(user, partner);
        return ResponseEntity.ok(new InviteResponse("초대를 보냈습니다.", connectionId));
    }

    @Operation(summary = "초대 수락하기", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{connectionId}/accept")
    public ResponseEntity<ApiResponse> acceptInvite(@PathVariable UUID connectionId,
                                        @AuthenticationPrincipal UserAccount user) {
        connectionService.acceptInvite(connectionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("초대를 수락했습니다."));
    } //ok

    @Operation(summary = "초대 거절하기", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{connectionId}/reject")
    public ResponseEntity<ApiResponse> rejectInvite(@PathVariable UUID connectionId,
                                        @AuthenticationPrincipal UserAccount user) {
        connectionService.rejectInvite(connectionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("초대를 거절했습니다."));
    } //ok

    @Operation(summary = "상대방과 연결 끊기", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{connectionId}/end")
    public ResponseEntity<ApiResponse> endConnection(@PathVariable UUID connectionId,
                                           @AuthenticationPrincipal UserAccount user) {
        connectionService.endConnection(connectionId, user.getId());
        return ResponseEntity.ok(new ApiResponse("연결이 종료되었습니다."));
    } //ok

    @Operation(summary = "원래 연결되었던 상대와 재연결", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/reconnect")
    public ResponseEntity<InviteResponse> reconnect(@RequestBody ReconnectRequest request,
                                       @AuthenticationPrincipal UserAccount user) {
        UUID connectionId = connectionService.reconnectByEmail(user.getId(), request.partnerEmail());
        return ResponseEntity.ok(new InviteResponse("재결합 요청을 보냈습니다.", connectionId));
    } //ok

    @GetMapping("/check")
    public ResponseEntity<ReconnectCheckResponse> checkConnection(
            @RequestParam String partnerEmail,
            @AuthenticationPrincipal UserAccount user) {

        boolean exists = connectionService.existsPreviousConnection(user, partnerEmail);

        return ResponseEntity.ok(new ReconnectCheckResponse(exists));
    }
}