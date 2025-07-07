package me.jinheum.datelog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.entity.UserAccount;
import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.enums.ConnectionStatus;

public interface UserConnectionRepository extends JpaRepository<UserConnection , UUID> {

    boolean existsByUserOrPartner(UserAccount user, UserAccount partner);

    Optional<UserConnection> findByUserAndPartner(UserAccount user, UserAccount partner);

    Optional<UserConnection> findByUserOrPartner(UserAccount user, UserAccount partner);

    List<UserConnection> findAllByPartnerAndStatus(UserAccount partner, ConnectionStatus status);
    
}
