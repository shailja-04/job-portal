package com.jobportal.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobportal.model.JobApplication;

@Repository
public interface JobApplicationRepo extends JpaRepository<JobApplication, Integer> {

    // seeker side: everything one email has applied to, newest first
    List<JobApplication> findByApplicantEmailOrderByAppliedAtDesc(String applicantEmail);

    // poster side: everyone who applied to one job, newest first
    List<JobApplication> findByJob_PostIdOrderByAppliedAtDesc(int jobId);

    // one person cannot apply twice to the same job with the same email
    boolean existsByJob_PostIdAndApplicantEmail(int jobId, String applicantEmail);

    // lookup by generated tracking reference number
    java.util.Optional<JobApplication> findByApplicationNumber(String applicationNumber);
}
