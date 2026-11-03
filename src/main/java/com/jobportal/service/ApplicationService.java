package com.jobportal.service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.dto.ApplicationResponse;
import com.jobportal.model.ApplicationStatus;
import com.jobportal.model.AppUser;
import com.jobportal.model.JobApplication;
import com.jobportal.model.JobPost;
import com.jobportal.repo.JobApplicationRepo;
import com.jobportal.repo.JobRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepo applicationRepo;
    private final JobRepo jobRepo;

    @Transactional
    public ApplicationResponse apply(int jobId, ApplicationRequest request, AppUser applicant) {
        JobPost job = jobRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + jobId + " not found"));

        if (applicationRepo.existsByJob_PostIdAndApplicantEmail(jobId, applicant.getEmail())) {
            throw new IllegalArgumentException("You have already applied to this job with this email");
        }

        int randomSuffix = (int) (Math.random() * 9000) + 1000;
        String appNumber = "APP-" + Year.now().getValue() + "-" + randomSuffix;

        JobApplication application = new JobApplication();
        application.setApplicationNumber(appNumber);
        application.setJob(job);
        application.setApplicant(applicant);
        application.setApplicantName(request.getApplicantName());
        application.setApplicantEmail(applicant.getEmail());
        application.setResumeUrl(request.getResumeUrl());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());

        JobApplication saved = applicationRepo.save(application);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByEmail(String email) {
        return applicationRepo.findByApplicantEmailOrderByAppliedAtDesc(email).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByJob(int jobId, AppUser poster) {
        JobPost job = jobRepo.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + jobId + " not found"));

        // Only the poster who created the job can view applicants
        if (job.getPostedBy() != null && job.getPostedBy().getId() != poster.getId()) {
            throw new IllegalArgumentException("You are not authorized to view applications for this job");
        }

        return applicationRepo.findByJob_PostIdOrderByAppliedAtDesc(jobId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateStatus(int id, ApplicationStatus status, AppUser poster) {
        JobApplication application = applicationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application with ID " + id + " not found"));

        // verify poster owns this job
        JobPost job = application.getJob();
        if (job.getPostedBy() != null && job.getPostedBy().getId() != poster.getId()) {
            throw new IllegalArgumentException("You are not authorized to update applications for this job");
        }

        application.setStatus(status);
        application.setUpdatedAt(LocalDateTime.now());
        JobApplication updated = applicationRepo.save(application);
        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse trackByNumber(String applicationNumber) {
        JobApplication application = applicationRepo.findByApplicationNumber(applicationNumber)
                .orElseThrow(() -> new IllegalArgumentException("No application found with reference number: " + applicationNumber));
        return mapToResponse(application);
    }

    public ApplicationResponse mapToResponse(JobApplication app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .jobId(app.getJob() != null ? app.getJob().getPostId() : 0)
                .jobProfile(app.getJob() != null ? app.getJob().getPostProfile() : "N/A")
                .applicantName(app.getApplicantName())
                .applicantEmail(app.getApplicantEmail())
                .resumeUrl(app.getResumeUrl())
                .status(app.getStatus())
                .statusLabel(app.getStatus() != null ? app.getStatus().getLabel() : null)
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
