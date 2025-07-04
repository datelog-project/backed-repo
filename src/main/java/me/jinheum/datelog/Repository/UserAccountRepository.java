package me.jinheum.datelog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID>{
    Optional<UserAccount> findByNameAndTag(String name, String tag);
    Optional<UserAccount> findByEmail(String email);
}
