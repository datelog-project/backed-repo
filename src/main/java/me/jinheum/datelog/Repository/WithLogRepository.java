package me.jinheum.datelog.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.entity.WithLog;

public interface WithLogRepository extends JpaRepository<WithLog, UUID> {
}