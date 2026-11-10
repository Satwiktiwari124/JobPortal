package com.example.jobPortal.service;

import java.util.List;

import com.example.jobPortal.dto.JobRequestDTO;
import com.example.jobPortal.dto.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.JobNotFoundException;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobService(
            JobRepository jobRepository,
            UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    // Create Job
    @Transactional
    public JobResponseDTO saveJob(JobRequestDTO jobRequestDTO) {

        String username = getAuthenticatedUsername();

        User recruiter = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Recruiter not found: " + username
                ));

        Job job = new Job();
        job.setTitle(jobRequestDTO.getTitle());
        job.setCompany(jobRequestDTO.getCompany());
        job.setLocation(jobRequestDTO.getLocation());
        job.setDescription(jobRequestDTO.getDescription());
        job.setRecruiter(recruiter);

        Job savedJob = jobRepository.save(job);

        return new JobResponseDTO(
                savedJob.getId(),
                savedJob.getTitle(),
                savedJob.getCompany(),
                savedJob.getLocation(),
                savedJob.getDescription(),
                recruiter.getId(),
                recruiter.getUsername()
        );
    }

    // Get All Jobs
    public List<Job> getAllJobs() {
        return jobRepository.findAllWithRecruiter();
    }

    // Get Job By ID
    public Job getJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new JobNotFoundException(
                        "Job not found with id: " + id
                ));
    }

    // Update Job - only its owner
    @Transactional
    public Job updateJob(Long id, Job job) {

        String username = getAuthenticatedUsername();

        Job existingJob = jobRepository.findById(id)
                .orElse(null);

        if (existingJob == null) {
            return null;
        }

        if (existingJob.getRecruiter() == null
                || !existingJob.getRecruiter()
                .getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can update only your own jobs"
            );
        }

        existingJob.setTitle(job.getTitle());
        existingJob.setCompany(job.getCompany());
        existingJob.setLocation(job.getLocation());
        existingJob.setDescription(job.getDescription());

        return jobRepository.save(existingJob);
    }

    // Delete Job - only its owner
    @Transactional
    public boolean deleteJob(Long id) {

        String username = getAuthenticatedUsername();

        Job existingJob = jobRepository.findById(id)
                .orElse(null);

        if (existingJob == null) {
            return false;
        }

        if (existingJob.getRecruiter() == null
                || !existingJob.getRecruiter()
                .getUsername().equals(username)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can delete only your own jobs"
            );
        }

        jobRepository.delete(existingJob);
        return true;
    }

    // Get current authenticated username
    private String getAuthenticatedUsername() {
        return SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();
    }
}
