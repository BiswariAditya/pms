package com.biswariadi.billing_service.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PatientEventsConsumer {

    @KafkaListener(topics = "patient", groupId = "billing-service")
    public void onPatientEvent(String message) {
        log.info("Received patient event: {}", message);
    }
}

