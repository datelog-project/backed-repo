package me.jinheum.datelog.repository;

import java.util.List;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;

import me.jinheum.datelog.entity.Media;

public interface MediaRepository extends JpaRepository<Media, UUID> {
    List<Media> findByWithLogId(UUID withLogId);
}