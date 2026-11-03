package com.jobportal.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jobportal.dto.ApiResponse;
import com.jobportal.dto.JobPostRequest;
import com.jobportal.dto.JobPostResponse;
import com.jobportal.model.AppUser;
import com.jobportal.service.AppUserService;
import com.jobportal.service.JobService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Endpoints for posting, browsing, and managing job postings")
public class JobRestController {

    private final JobService jobService;
    private final AppUserService userService;

    @PostMapping
    @Operation(summary = "Post a new job (Posters only)", description = "Creates a new job listing associated with the authenticated poster.")
    public ResponseEntity<ApiResponse<JobPostResponse>> createJob(
            @Valid @RequestBody JobPostRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser poster = userService.findByEmail(userDetails.getUsername());
        JobPostResponse response = jobService.createJob(request, poster);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all jobs (Public)", description = "Retrieves a list of all active job posts.")
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getAllJobs() {
        List<JobPostResponse> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched successfully", jobs));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job details by ID (Public)", description = "Returns full details for a single job.")
    public ResponseEntity<ApiResponse<JobPostResponse>> getJobById(@PathVariable int id) {
        JobPostResponse job = jobService.getJobResponseById(id);
        return ResponseEntity.ok(ApiResponse.success("Job details retrieved", job));
    }

    @GetMapping("/my-posted")
    @Operation(summary = "Get jobs posted by current poster", description = "Returns all jobs created by the authenticated recruiter/poster.")
    public ResponseEntity<ApiResponse<List<JobPostResponse>>> getMyPostedJobs(
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser poster = userService.findByEmail(userDetails.getUsername());
        List<JobPostResponse> jobs = jobService.getJobsByPoster(poster.getId());
        return ResponseEntity.ok(ApiResponse.success("Your posted jobs retrieved", jobs));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job post (Poster only)", description = "Deletes a job listing. Only the creator of the job is authorized to delete it.")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable int id,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUser requester = userService.findByEmail(userDetails.getUsername());
        jobService.deleteJob(id, requester);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }
}
