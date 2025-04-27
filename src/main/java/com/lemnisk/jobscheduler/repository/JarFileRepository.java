package com.lemnisk.jobscheduler.repository;

import com.lemnisk.jobscheduler.model.JarFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JarFileRepository extends JpaRepository<JarFile, UUID> {
    boolean existsByName(String name);
}
