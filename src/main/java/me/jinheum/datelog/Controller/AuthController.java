package me.jinheum.datelog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.AccessTokenResponse;
import me.jinheum.datelog.service.AuthService;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;


    @PostMapping("/reissue")
    public ResponseEntity<AccessTokenResponse> reissue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = authService.reissue(request, response);
        return ResponseEntity.ok(new AccessTokenResponse(accessToken));
    }
}
