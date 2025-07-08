package me.jinheum.datelog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;
import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
public interface UserConnectionRepository extends JpaRepository<UserConnection , UUID> {

    List<UserConnection> findAllByUserOrPartner(UserAccount user1, UserAccount partner);
    
    @Query("SELECT uc FROM UserConnection uc WHERE " +
        "(uc.user = :user1 AND uc.partner = :user2) OR " +
        "(uc.user = :user2 AND uc.partner = :user1)")
    Optional<UserConnection> findByUserAndPartner(@Param("user1") UserAccount user1, @Param("user2") UserAccount user2);
    
}
