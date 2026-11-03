package com.jobportal.dto;

import com.jobportal.model.ApplicationStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "Status is required (APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED)")
    private ApplicationStatus status;
}
