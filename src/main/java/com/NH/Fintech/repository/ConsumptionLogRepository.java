package com.NH.Fintech.repository;

import com.NH.Fintech.domain.consumption.ConsumptionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ConsumptionLogRepository extends JpaRepository<ConsumptionLog, Long> {
    List<ConsumptionLog> findByUserId(Long userId);
    List<ConsumptionLog> findByUserIdAndTransactionAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
