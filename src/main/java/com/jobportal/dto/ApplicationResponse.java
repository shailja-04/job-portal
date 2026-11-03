package com.jobportal.dto;

import java.time.LocalDateTime;

import com.jobportal.model.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private int id;
    private String applicationNumber;
    private int jobId;
    private String jobProfile;
    private String applicantName;
    private String applicantEmail;
    private String resumeUrl;
    private ApplicationStatus status;
    private String statusLabel;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
