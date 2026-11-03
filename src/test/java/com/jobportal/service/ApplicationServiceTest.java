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

import com.jobportal.dto.ApplicationRequest;
import com.jobportal.dto.ApplicationResponse;
import com.jobportal.model.ApplicationStatus;
import com.jobportal.model.AppUser;
import com.jobportal.model.JobApplication;
import com.jobportal.model.JobPost;
import com.jobportal.model.Role;
import com.jobportal.repo.JobApplicationRepo;
import com.jobportal.repo.JobRepo;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private JobApplicationRepo applicationRepo;

    @Mock
    private JobRepo jobRepo;

    @InjectMocks
    private ApplicationService applicationService;

    private AppUser seeker;
    private AppUser poster;
    private JobPost job;
    private JobApplication application;

    @BeforeEach
    void setUp() {
        poster = new AppUser();
        poster.setId(1);
        poster.setName("Poster User");
        poster.setEmail("poster@jobportal.com");
        poster.setRole(Role.POSTER);

        seeker = new AppUser();
        seeker.setId(2);
        seeker.setName("Seeker User");
        seeker.setEmail("seeker@jobportal.com");
        seeker.setRole(Role.SEEKER);

        job = new JobPost();
        job.setPostId(10);
        job.setPostProfile("Backend Engineer");
        job.setPostedBy(poster);

        application = new JobApplication();
        application.setId(100);
        application.setApplicationNumber("APP-2026-9999");
        application.setJob(job);
        application.setApplicant(seeker);
        application.setApplicantName("Seeker User");
        application.setApplicantEmail("seeker@jobportal.com");
        application.setResumeUrl("https://example.com/resume.pdf");
        application.setStatus(ApplicationStatus.APPLIED);
        application.setAppliedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("apply should successfully submit an application and return response")
    void testApply_Success() {
        ApplicationRequest request = new ApplicationRequest();
        request.setApplicantName("Seeker User");
        request.setResumeUrl("https://example.com/resume.pdf");

        when(jobRepo.findById(10)).thenReturn(Optional.of(job));
        when(applicationRepo.existsByJob_PostIdAndApplicantEmail(10, seeker.getEmail())).thenReturn(false);
        when(applicationRepo.save(any(JobApplication.class))).thenReturn(application);

        ApplicationResponse response = applicationService.apply(10, request, seeker);

        assertNotNull(response);
        assertEquals("APP-2026-9999", response.getApplicationNumber());
        assertEquals("seeker@jobportal.com", response.getApplicantEmail());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(applicationRepo, times(1)).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("apply should throw exception if candidate already applied to the same job")
    void testApply_DuplicateApplication() {
        ApplicationRequest request = new ApplicationRequest();
        request.setApplicantName("Seeker User");

        when(jobRepo.findById(10)).thenReturn(Optional.of(job));
        when(applicationRepo.existsByJob_PostIdAndApplicantEmail(10, seeker.getEmail())).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            applicationService.apply(10, request, seeker);
        });

        assertTrue(ex.getMessage().contains("already applied"));
        verify(applicationRepo, never()).save(any());
    }

    @Test
    @DisplayName("getByJob should allow job owner poster to view applicants")
    void testGetByJob_Authorized() {
        when(jobRepo.findById(10)).thenReturn(Optional.of(job));
        when(applicationRepo.findByJob_PostIdOrderByAppliedAtDesc(10)).thenReturn(List.of(application));

        List<ApplicationResponse> results = applicationService.getByJob(10, poster);

        assertEquals(1, results.size());
        assertEquals("APP-2026-9999", results.get(0).getApplicationNumber());
    }

    @Test
    @DisplayName("getByJob should deny access if another poster tries to view applicants")
    void testGetByJob_UnauthorizedPoster() {
        AppUser otherPoster = new AppUser();
        otherPoster.setId(99);
        otherPoster.setRole(Role.POSTER);

        when(jobRepo.findById(10)).thenReturn(Optional.of(job));

        assertThrows(IllegalArgumentException.class, () -> {
            applicationService.getByJob(10, otherPoster);
        });
    }

    @Test
    @DisplayName("updateStatus should update status when performed by job owner")
    void testUpdateStatus_Success() {
        when(applicationRepo.findById(100)).thenReturn(Optional.of(application));
        when(applicationRepo.save(any(JobApplication.class))).thenReturn(application);

        ApplicationResponse response = applicationService.updateStatus(100, ApplicationStatus.SHORTLISTED, poster);

        assertNotNull(response);
        assertEquals(ApplicationStatus.SHORTLISTED, application.getStatus());
        verify(applicationRepo, times(1)).save(application);
    }

    @Test
    @DisplayName("trackByNumber should return application by reference code")
    void testTrackByNumber_Success() {
        when(applicationRepo.findByApplicationNumber("APP-2026-9999")).thenReturn(Optional.of(application));

        ApplicationResponse response = applicationService.trackByNumber("APP-2026-9999");

        assertNotNull(response);
        assertEquals("APP-2026-9999", response.getApplicationNumber());
    }
}
