package com.biswariadi.patient_services.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatientRequestDTO {
    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;
    @NotBlank(message = "Address is mandatory")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
    @NotBlank(message = "Date of birth is mandatory")
    private String date_of_birth;
    @NotBlank(message = "Registered date is mandatory")
    private String registered_date;
}
