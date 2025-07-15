package me.jinheum.datelog.service;


import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.SignupRequest;
import me.jinheum.datelog.dto.SignupResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.exception.EmailAlreadyExistsException;
import me.jinheum.datelog.repository.UserAccountRepository;


@Service
@RequiredArgsConstructor
public class UserAccountService {
    
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;


    public SignupResponse signup(SignupRequest request) {
        if (userAccountRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("이미 존재하는 이메일입니다.");
        } //ok

        String hashedPassword = passwordEncoder.encode(request.password());

        UserAccount user = UserAccount.builder()
                .name(request.name())
                .email(request.email())
                .password(hashedPassword)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        return new SignupResponse(saved.getId());
    }

    public UserAccount getUserById(UUID id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
    } //ok

    public UserAccount getUserByEmail(String email) {
        return userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 유저가 존재하지 않습니다."));
    } //ok
}
