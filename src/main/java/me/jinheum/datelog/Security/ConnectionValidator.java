package me.jinheum.datelog.security;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.repository.UserConnectionRepository;

@Component
@RequiredArgsConstructor
public class ConnectionValidator {

    private final UserConnectionRepository userConnectionRepository;

    public void validateInvite(UserAccount inviter, UserAccount partner) {
        if (inviter.getEmail().equals(partner.getEmail())) {
            throw new IllegalArgumentException("본인에게 초대할 수 없습니다.");
        }

        if (userConnectionRepository.findByUserAndPartner(partner, inviter).isPresent()) {
            throw new IllegalStateException("상대방이 이미 나를 초대했습니다. 받은 초대를 수락해주세요.");
        }

        if (userConnectionRepository.existsByUserOrPartner(inviter, inviter)) {
            throw new IllegalStateException("이미 연결된 유저가 있습니다.");
        }
    }
}
