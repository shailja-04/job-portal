package com.jobportal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.ApiResponse;
import com.jobportal.dto.ApplicationRequest;
import com.jobportal.dto.ApplicationResponse;
import com.jobportal.dto.StatusUpdateRequest;
import com.jobportal.model.AppUser;
import com.jobportal.service.AppUserService;
import com.jobportal.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Endpoints for applying to jobs and tracking application workflows")
public class ApplicationRestController {

    private final ApplicationService applicationService;
    private final AppUserService userService;

    @PostMapping("/jobs/{jobId}/apply")
    @Operation(summary = "Apply for a job (Seekers only)", description = "Submits an application for the specified job using the authenticated seeker account.")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @PathVariable int jobId,
            @Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser applicant = userService.findByEmail(userDetails.getUsername());
        ApplicationResponse response = applicationService.apply(jobId, request, applicant);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    @GetMapping("/applications/my")
    @Operation(summary = "Get my applications (Seekers only)", description = "Retrieves all applications submitted by the logged-in job seeker.")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<ApplicationResponse> list = applicationService.getByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", list));
    }

    @GetMapping("/jobs/{jobId}/applications")
    @Operation(summary = "Get applicants for a job (Posters only)", description = "Retrieves all applicants for a job post. Only authorized for the job owner.")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getApplicantsForJob(
            @PathVariable int jobId,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser poster = userService.findByEmail(userDetails.getUsername());
        List<ApplicationResponse> applicants = applicationService.getByJob(jobId, poster);
        return ResponseEntity.ok(ApiResponse.success("Applicants retrieved successfully", applicants));
    }

    @PatchMapping("/applications/{id}/status")
    @Operation(summary = "Update application status (Posters only)", description = "Updates candidate status (APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED).")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable int id,
            @Valid @RequestBody StatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser poster = userService.findByEmail(userDetails.getUsername());
        ApplicationResponse response = applicationService.updateStatus(id, request.getStatus(), poster);
        return ResponseEntity.ok(ApiResponse.success("Status updated to " + request.getStatus(), response));
    }

    @GetMapping("/applications/track/{applicationNumber}")
    @Operation(summary = "Track application by reference number (Public)", description = "Public tracking lookup by unique reference number (e.g. APP-2026-4821).")
    public ResponseEntity<ApiResponse<ApplicationResponse>> trackByNumber(
            @PathVariable String applicationNumber) {

        ApplicationResponse response = applicationService.trackByNumber(applicationNumber);
        return ResponseEntity.ok(ApiResponse.success("Application tracking status retrieved", response));
    }
}
