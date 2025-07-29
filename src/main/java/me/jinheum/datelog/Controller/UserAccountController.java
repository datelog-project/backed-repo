package me.jinheum.datelog.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.SignupRequest;
import me.jinheum.datelog.dto.SignupResponse;
import me.jinheum.datelog.dto.UserInfoResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.service.UserAccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserAccountController {
    
    private final UserAccountService userAccountService;
    
    @Operation(summary = "회원가입")
    @PostMapping("/signup") //회원가입
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        SignupResponse response = userAccountService.signup(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUserInfo(@AuthenticationPrincipal UserAccount userAccount) {
        UUID userId = userAccount.getId();
        UserInfoResponse response = userAccountService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }
    

}
