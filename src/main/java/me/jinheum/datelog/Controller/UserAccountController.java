package me.jinheum.datelog.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.SigninRequest;
import me.jinheum.datelog.dto.SigninResponse;
import me.jinheum.datelog.dto.SignupRequest;
import me.jinheum.datelog.dto.SignupResponse;
import me.jinheum.datelog.dto.signoutMessageResponse;
import me.jinheum.datelog.security.CustomUserDetails;
import me.jinheum.datelog.service.UserAccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserAccountController {
    
    private final UserAccountService userAccountService;
    
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        SignupResponse response = userAccountService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponse> signin(@RequestBody SigninRequest request,
                                               HttpServletResponse response) {
        SigninResponse loginResponse = userAccountService.signin(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signout")
    public ResponseEntity<signoutMessageResponse> signout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {

        UUID userId = userDetails.getUserId();

        userAccountService.signout(userId, response);

        return ResponseEntity.ok(new signoutMessageResponse("로그아웃 성공"));
    }
    
}
