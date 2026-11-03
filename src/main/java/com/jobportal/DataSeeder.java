package com.jobportal;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.jobportal.model.AppUser;
import com.jobportal.model.JobPost;
import com.jobportal.model.Role;
import com.jobportal.repo.AppUserRepo;
import com.jobportal.repo.JobRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final JobRepo repo;
    private final AppUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Seed default accounts if empty
        AppUser defaultPoster = userRepo.findByEmail("poster@jobportal.com").orElse(null);
        if (defaultPoster == null) {
            defaultPoster = userRepo.save(new AppUser(0, "Acme Hiring Team", "poster@jobportal.com",
                    passwordEncoder.encode("password123"), Role.POSTER));
        }

        if (userRepo.findByEmail("seeker@jobportal.com").isEmpty()) {
            userRepo.save(new AppUser(0, "John Candidate", "seeker@jobportal.com",
                    passwordEncoder.encode("password123"), Role.SEEKER));
        }

        // seed sample jobs only when the table is empty
        if (repo.count() == 0) {
            final AppUser poster = defaultPoster;
            List<JobPost> sampleJobs = List.of(
                    new JobPost(0, "Java Developer", "Must have good experience in core Java and advanced Java", 2,
                            List.of("Core Java", "J2EE", "Spring Boot", "Hibernate")),
                    new JobPost(0, "Frontend Developer", "Experience in building responsive web applications using React", 3,
                            List.of("HTML", "CSS", "JavaScript", "React")),
                    new JobPost(0, "Data Scientist", "Strong background in machine learning and data analysis", 4,
                            List.of("Python", "Machine Learning", "Data Analysis")),
                    new JobPost(0, "Network Engineer", "Design and implement computer networks for efficient data communication", 5,
                            List.of("Networking", "Cisco", "Routing", "Switching")),
                    new JobPost(0, "Mobile App Developer", "Experience in mobile app development for iOS and Android", 3,
                            List.of("iOS Development", "Android Development", "Mobile App"))
            );
            sampleJobs.forEach(job -> job.setPostedBy(poster));
            repo.saveAll(sampleJobs);
        }
    }
}

