package me.jinheum.datelog.controller;


import org.springframework.http.ResponseEntity;
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
import me.jinheum.datelog.service.UserAccountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class UserAccountController {
    
    private final UserAccountService userAccountService;
    
    @PostMapping("/signup") //회원가입
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        SignupResponse response = userAccountService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin") //로그인
    public ResponseEntity<SigninResponse> signin(@RequestBody SigninRequest request,
                                               HttpServletResponse response) {
        SigninResponse loginResponse = userAccountService.signin(request, response);
        return ResponseEntity.ok(loginResponse);
    }
    
}
