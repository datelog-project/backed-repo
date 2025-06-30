package me.jinheum.datelog.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.Entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID>{
    Optional<UserAccount> findByNameAndTag(String name, String tag);
}
