package me.jinheum.datelog.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.DTO.SigninRequest;
import me.jinheum.datelog.DTO.SigninResponse;
import me.jinheum.datelog.DTO.SignupRequest;
import me.jinheum.datelog.DTO.SignupResponse;
import me.jinheum.datelog.Service.UserAccountService;

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
    public ResponseEntity<SigninResponse> login(@RequestBody SigninRequest request,
                                               HttpServletResponse response) {
        SigninResponse loginResponse = userAccountService.login(request, response);
        return ResponseEntity.ok(loginResponse);
    }

    
}
