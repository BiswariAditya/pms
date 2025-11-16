package com.biswariadi.patient_services.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientResponseDTO {
    private String id;
    private String name;
    private String email;
    private String address;
    private String date_of_birth;

    /// registered_date is for db use only no need to expose on  frontend



}
