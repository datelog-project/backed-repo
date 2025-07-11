package me.jinheum.datelog.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.entity.UserConnection;
import me.jinheum.datelog.entity.WithLog;

public interface WithLogRepository extends JpaRepository<WithLog, UUID> {
    List<WithLog> findByUserConnectionOrderByDateDesc(UserConnection connection);
}