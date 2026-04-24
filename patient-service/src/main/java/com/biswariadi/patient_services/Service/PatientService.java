package com.biswariadi.patient_services.Service;

import com.biswariadi.patient_services.DTO.PatientRequestDTO;
import com.biswariadi.patient_services.DTO.PatientResponseDTO;
import com.biswariadi.patient_services.grpc.BillingServiceGrpcClient;
import com.biswariadi.patient_services.kafka.kafkaProducer;
import com.biswariadi.patient_services.mapper.PatientMapper;
import com.biswariadi.patient_services.model.Patient;
import com.biswariadi.patient_services.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    BillingServiceGrpcClient billingServiceGrpcClient;

    @Autowired
    kafkaProducer kafkaProducer;

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
        ///replaced lambda with method reference this is more readable. It is read as "map toDTO method of PatientMapper class"
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        Patient patient = PatientMapper.toEntity(patientRequestDTO);
        Patient savedPatient = patientRepository.save(patient);
        // Call gRPC service to create billing account
        billingServiceGrpcClient.createBillingAccount(
                savedPatient.getId().toString(),
                savedPatient.getName(),
                savedPatient.getEmail()
        );
        kafkaProducer.sendEvent(savedPatient);
        return PatientMapper.toDTO(savedPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        if (!existingPatient.getEmail().equals(patientRequestDTO.getEmail()) &&
                patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        existingPatient.setName(patientRequestDTO.getName());
        existingPatient.setEmail(patientRequestDTO.getEmail());
        existingPatient.setAddress(patientRequestDTO.getAddress());
        existingPatient.setDate_of_birth(java.time.LocalDate.parse(patientRequestDTO.getDate_of_birth()));
        // Assuming registered_date should not be updated

        Patient updatedPatient = patientRepository.save(existingPatient);
        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Patient not found");
        }
        patientRepository.deleteById(id);
    }
}
