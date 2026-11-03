package com.jobportal.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.jobportal.dto.JobPostRequest;
import com.jobportal.dto.JobPostResponse;
import com.jobportal.model.AppUser;
import com.jobportal.model.JobPost;
import com.jobportal.model.Role;
import com.jobportal.repo.JobRepo;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepo jobRepo;

    @InjectMocks
    private JobService jobService;

    private AppUser demoPoster;
    private JobPost demoPost;

    @BeforeEach
    void setUp() {
        demoPoster = new AppUser();
        demoPoster.setId(1);
        demoPoster.setName("Demo Poster");
        demoPoster.setEmail("poster@jobportal.com");
        demoPoster.setRole(Role.POSTER);

        demoPost = new JobPost();
        demoPost.setPostId(101);
        demoPost.setPostProfile("Java Developer");
        demoPost.setPostDesc("Develop scalable microservices with Spring Boot");
        demoPost.setReqExperience(2);
        demoPost.setPostTechStack(List.of("Java", "Spring Boot", "PostgreSQL"));
        demoPost.setPostedBy(demoPoster);
        demoPost.setCreatedAt(LocalDateTime.now());
        demoPost.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createJob should save job and return mapped JobPostResponse")
    void testCreateJob_Success() {
        JobPostRequest request = new JobPostRequest();
        request.setPostProfile("Java Developer");
        request.setPostDesc("Develop scalable microservices with Spring Boot");
        request.setReqExperience(2);
        request.setPostTechStack(List.of("Java", "Spring Boot", "PostgreSQL"));

        when(jobRepo.save(any(JobPost.class))).thenReturn(demoPost);

        JobPostResponse response = jobService.createJob(request, demoPoster);

        assertNotNull(response);
        assertEquals(101, response.getPostId());
        assertEquals("Java Developer", response.getPostProfile());
        assertEquals(2, response.getReqExperience());
        assertEquals("poster@jobportal.com", response.getPostedByEmail());
        verify(jobRepo, times(1)).save(any(JobPost.class));
    }

    @Test
    @DisplayName("getAllJobs should return all mapped job postings")
    void testGetAllJobs_Success() {
        when(jobRepo.findAll()).thenReturn(List.of(demoPost));

        List<JobPostResponse> results = jobService.getAllJobs();

        assertEquals(1, results.size());
        assertEquals("Java Developer", results.get(0).getPostProfile());
        verify(jobRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("getJobResponseById should return job when found")
    void testGetJobResponseById_Found() {
        when(jobRepo.findById(101)).thenReturn(Optional.of(demoPost));

        JobPostResponse response = jobService.getJobResponseById(101);

        assertNotNull(response);
        assertEquals(101, response.getPostId());
        assertEquals("Java Developer", response.getPostProfile());
    }

    @Test
    @DisplayName("getJobResponseById should throw exception when job not found")
    void testGetJobResponseById_NotFound() {
        when(jobRepo.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> jobService.getJobResponseById(999));
    }

    @Test
    @DisplayName("deleteJob should successfully delete if user is the poster")
    void testDeleteJob_Success() {
        when(jobRepo.findById(101)).thenReturn(Optional.of(demoPost));

        jobService.deleteJob(101, demoPoster);

        verify(jobRepo, times(1)).delete(demoPost);
    }

    @Test
    @DisplayName("deleteJob should throw exception if requester is not the job poster")
    void testDeleteJob_ForbiddenRequester() {
        AppUser anotherPoster = new AppUser();
        anotherPoster.setId(99);
        anotherPoster.setEmail("hacker@other.com");
        anotherPoster.setRole(Role.POSTER);

        when(jobRepo.findById(101)).thenReturn(Optional.of(demoPost));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            jobService.deleteJob(101, anotherPoster);
        });

        assertTrue(ex.getMessage().contains("You can only delete jobs that you posted"));
        verify(jobRepo, never()).delete(any());
    }
}
