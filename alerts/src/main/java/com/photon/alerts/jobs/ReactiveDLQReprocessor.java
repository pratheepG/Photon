package com.photon.alerts.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photon.alerts.entity.Alert;
import com.photon.alerts.repository.DeadLetterAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ReactiveDLQReprocessor {
//
//    private final DeadLetterAlertRepository dlqRepo;
//    private final AlertPublisher alertPublisher;
//    private final ObjectMapper objectMapper;
//
//    @Scheduled(cron = "0 */5 * * * *") // every 5 mins
//    public void retryDLQAlerts() {
//        dlqRepo.findAllByManuallyProcessedFalse()
//            .flatMap(deadAlert -> {
//                try {
//                    Alert alert = objectMapper.readValue(deadAlert.getOriginalAlertJson(), Alert.class);
//                    return alertPublisher.publish(alert, true) // retry = true
//                        .then(dlqRepo.save(deadAlert.toBuilder().manuallyProcessed(true).build()));
//                } catch (Exception e) {
//                    log.error("DLQ parse failed for alert: {}", deadAlert.getId(), e);
//                    return Mono.empty(); // swallow error to keep flow alive
//                }
//            })
//            .subscribe();
//    }
//}