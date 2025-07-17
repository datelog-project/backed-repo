package me.jinheum.datelog.service;


import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.dto.SignupRequest;
import me.jinheum.datelog.dto.SignupResponse;
import me.jinheum.datelog.dto.UserInfoResponse;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.enums.ConnectionStatus;
import me.jinheum.datelog.exception.EmailAlreadyExistsException;
import me.jinheum.datelog.repository.UserAccountRepository;
import me.jinheum.datelog.repository.UserConnectionRepository;


@Service
@RequiredArgsConstructor
public class UserAccountService {
    
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserConnectionRepository userConnectionRepository;


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

    
    
    public UserInfoResponse getUserInfo(UUID userId) {
        UserAccount loginUser = userAccountRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        List<UserConnection> connections = userConnectionRepository.findByUserOrPartner(loginUser);

        if (connections.isEmpty()) {
            return new UserInfoResponse(
                loginUser.getId(),
                null,
                null,
                loginUser.getId(),
                null,
                null,
                null,
                false
            );
        }

        UserConnection selected = connections.stream()
            .filter(conn -> conn.getStatus() == ConnectionStatus.CONNECTED)
            .findFirst()
            .orElseGet(() ->
                connections.stream()
                    .filter(conn -> conn.getStatus() == ConnectionStatus.PENDING)
                    .findFirst()
                    .orElseGet(() ->
                        connections.stream()
                            .filter(conn -> conn.getStatus() == ConnectionStatus.ENDED)
                            .findFirst()
                            .orElse(null)
                    )
            );

        if (selected == null) {
            return new UserInfoResponse(
                loginUser.getId(),
                null,
                null,
                loginUser.getId(),
                null,
                null,
                null,
                false
            );
        }

        boolean isSender = selected.getUser().getId().equals(loginUser.getId());
        UserAccount partner = isSender ? selected.getPartner() : selected.getUser();

        return new UserInfoResponse(
            loginUser.getId(),
            selected.getId(),
            selected.getStatus().name(),
            loginUser.getId(),
            partner.getId(),
            partner.getName(),
            partner.getEmail(),
            isSender
        );
}




}
