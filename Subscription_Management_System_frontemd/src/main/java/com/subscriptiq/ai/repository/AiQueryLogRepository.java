package com.subscriptiq.ai.repository;

import com.subscriptiq.ai.entity.AiQueryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiQueryLogRepository extends JpaRepository<AiQueryLog, Long> {

    Page<AiQueryLog> findByUserId(Long userId, Pageable pageable);

    List<AiQueryLog> findByDetectedIntent(String detectedIntent);
}

