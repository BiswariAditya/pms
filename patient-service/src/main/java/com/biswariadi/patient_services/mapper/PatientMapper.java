package com.biswariadi.patient_services.mapper;

import com.biswariadi.patient_services.DTO.PatientRequestDTO;
import com.biswariadi.patient_services.DTO.PatientResponseDTO;
import com.biswariadi.patient_services.model.Patient;

public class PatientMapper {

    public static PatientResponseDTO toDTO(Patient patient) {
        PatientResponseDTO dto = new PatientResponseDTO();
        dto.setId(patient.getId().toString());
        dto.setName(patient.getName());
        dto.setEmail(patient.getEmail());
        dto.setAddress(patient.getAddress());
        dto.setDate_of_birth(patient.getDate_of_birth().toString());
        return dto;
    }

    public static Patient toEntity(PatientRequestDTO dto) {
        Patient patient = new Patient();
        patient.setName(dto.getName());
        patient.setEmail(dto.getEmail());
        patient.setAddress(dto.getAddress());
        patient.setDate_of_birth(java.time.LocalDate.parse(dto.getDate_of_birth()));
        patient.setRegistered_date(java.time.LocalDate.parse(dto.getRegistered_date()));
        return patient;
    }
}
