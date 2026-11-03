package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.dto.JobPostRequest;
import com.jobportal.dto.JobPostResponse;
import com.jobportal.model.AppUser;
import com.jobportal.model.JobPost;
import com.jobportal.repo.JobRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepo repo;

    @Transactional
    public JobPostResponse createJob(JobPostRequest request, AppUser poster) {
        JobPost jobPost = new JobPost();
        jobPost.setPostProfile(request.getPostProfile());
        jobPost.setPostDesc(request.getPostDesc());
        jobPost.setReqExperience(request.getReqExperience());
        jobPost.setPostTechStack(request.getPostTechStack());
        jobPost.setPostedBy(poster);
        jobPost.setCreatedAt(LocalDateTime.now());
        jobPost.setUpdatedAt(LocalDateTime.now());

        JobPost saved = repo.save(jobPost);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<JobPostResponse> getAllJobs() {
        return repo.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobPost getJobById(int id) {
        return repo.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public JobPostResponse getJobResponseById(int id) {
        JobPost post = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));
        return mapToResponse(post);
    }

    @Transactional(readOnly = true)
    public List<JobPostResponse> getJobsByPoster(int posterId) {
        return repo.findByPostedBy_IdOrderByCreatedAtDesc(posterId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteJob(int id, AppUser requester) {
        JobPost post = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job with ID " + id + " not found"));

        // verify ownership if poster is set
        if (post.getPostedBy() != null && post.getPostedBy().getId() != requester.getId()) {
            throw new IllegalArgumentException("You can only delete jobs that you posted");
        }
        repo.delete(post);
    }

    public JobPostResponse mapToResponse(JobPost post) {
        return JobPostResponse.builder()
                .postId(post.getPostId())
                .postProfile(post.getPostProfile())
                .postDesc(post.getPostDesc())
                .reqExperience(post.getReqExperience())
                .postTechStack(post.getPostTechStack())
                .postedByName(post.getPostedBy() != null ? post.getPostedBy().getName() : "Anonymous Recruiter")
                .postedByEmail(post.getPostedBy() != null ? post.getPostedBy().getEmail() : null)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
