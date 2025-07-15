package me.jinheum.datelog.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.entity.SharedLink;
import me.jinheum.datelog.entity.WithLog;

public interface SharedLinkRepository extends JpaRepository<SharedLink, UUID> {
    Optional<SharedLink> findByWithLog(WithLog withLog);
}
