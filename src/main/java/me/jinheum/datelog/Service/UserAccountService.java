package me.jinheum.datelog.Service;

import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.DTO.SignupRequest;
import me.jinheum.datelog.DTO.SignupResponse;
import me.jinheum.datelog.Entity.UserAccount;
import me.jinheum.datelog.Repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        String tag = generatedUniqueTagForName(request.name());
        String username = request.name() + "#" + tag;

        String hashedPassword = passwordEncoder.encode(request.password());

        UserAccount user = UserAccount.builder()
                .name(request.name())
                .tag(tag)
                .username(username)
                .email(request.email())
                .password(hashedPassword)
                .build();

        UserAccount saved = userAccountRepository.save(user);
        return new SignupResponse(saved.getId(), saved.getUsername());
    }


    public String generatedUniqueTagForName(String name) {
        Random random = new Random();
        String tag;
        int maxAttempts = 1000;

        for (int i = 0; i < maxAttempts; i++) {
            int randNum = random.nextInt(9000) + 1000;
            tag = String.valueOf(randNum);
            boolean exists = userAccountRepository.findByNameAndTag(name, tag).isPresent();
            if (!exists) return tag;
        }
        throw new RuntimeException("중복으로 인해 유효한 tag 생성 실패");
    }
}
