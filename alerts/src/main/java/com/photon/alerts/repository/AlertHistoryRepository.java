package com.photon.alerts.repository;

import com.photon.alerts.entity.AlertHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertHistoryRepository extends ReactiveCrudRepository<AlertHistory, Long> {}