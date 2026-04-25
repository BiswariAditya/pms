package com.biswariadi.patient_services.kafka;

import com.biswariadi.patient_services.model.Patient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class kafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void sendEvent(Patient patient){
        try{
            String payload = objectMapper.writeValueAsString(new PatientCreatedEvent(
                    patient.getId().toString(),
                    patient.getName(),
                    patient.getEmail(),
                    "PATIENT_CREATED"
            ));
            kafkaTemplate.send("patient", payload);
            log.info("Patient event sent to Kafka: {}", payload);
        } catch (JsonProcessingException e) {
            log.error("Error serializing patient event: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error sending patient event to Kafka: {}", e.getMessage());
        }
    }

    private record PatientCreatedEvent(String patientId, String name, String email, String eventType) {}
}
