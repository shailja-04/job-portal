package com.jobportal.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostRequest {

    @NotBlank(message = "Job profile is required")
    private String postProfile;

    @NotBlank(message = "Job description is required")
    private String postDesc;

    @NotNull(message = "Required experience is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer reqExperience;

    private List<String> postTechStack;
}
