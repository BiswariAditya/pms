package com.biswariadi.patient_services.kafka;

import com.biswariadi.patient_services.model.Patient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Slf4j
@Service
public class kafkaProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendEvent(Patient patient){
        PatientEvent patientEvent = PatientEvent.newBuilder()
                .setPatientId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PATIENT_CREATED")
                .build();
        try{
            kafkaTemplate.send("patient", patientEvent.toString());
            log.info("Patient event sent to Kafka: {}", patientEvent);
        } catch (Exception e) {
            log.error("Error sending patient event to Kafka: {}", e.getMessage());
        }
    }
}
