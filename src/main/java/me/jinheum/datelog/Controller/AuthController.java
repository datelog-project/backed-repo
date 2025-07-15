package me.jinheum.datelog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.AccessTokenResponse;
import me.jinheum.datelog.dto.SigninRequest;
import me.jinheum.datelog.dto.SigninResponse;
import me.jinheum.datelog.service.AuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;

    @PostMapping("/reissue") //리프레시 토큰 재발급
    public ResponseEntity<AccessTokenResponse> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = authService.reissue(request, response);
        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }

    @PostMapping("/signin") //로그인
    public ResponseEntity<SigninResponse> signin(@RequestBody SigninRequest request,
                                               HttpServletResponse response) {
        SigninResponse loginResponse = authService.signin(request, response);
        return ResponseEntity.ok(loginResponse);
    }
}