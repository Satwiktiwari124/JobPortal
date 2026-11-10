
package com.example.jobPortal.service;

import com.example.jobPortal.dto.JobApplicationResponseDTO;
import com.example.jobPortal.entity.ApplicationStatus;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.JobApplication;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.JobApplicationRepository;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            EmailService emailService
    ) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Transactional
    public JobApplicationResponseDTO applyForJob(Long jobId) {
        User candidate = getCurrentUser();

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Job not found: " + jobId));

        if (jobApplicationRepository.existsByJob_IdAndCandidate_Id(
                jobId, candidate.getId())) {
            throw new IllegalArgumentException(
                    "You have already applied for this job");
        }

        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setCandidate(candidate);
        application.setStatus(ApplicationStatus.APPLIED);

        JobApplication savedApplication =
                jobApplicationRepository.save(application);

        JobApplicationResponseDTO response =
                JobApplicationResponseDTO.from(savedApplication);

        String candidateEmail = candidate.getEmail();
        String candidateName = candidate.getUsername();
        String jobTitle = job.getTitle();
        String companyName = job.getCompany();
        String location = job.getLocation();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            emailService.sendApplicationConfirmation(
                                    candidateEmail,
                                    candidateName,
                                    jobTitle,
                                    companyName,
                                    location
                            );
                        } catch (Exception e) {
                            System.err.println(
                                    "Application confirmation email failed: "
                                            + e.getMessage()
                            );
                        }
                    }
                }
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseDTO> getMyApplications() {
        User candidate = getCurrentUser();

        return jobApplicationRepository
                .findByCandidate_IdOrderByAppliedAtDesc(candidate.getId())
                .stream()
                .map(JobApplicationResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobApplicationResponseDTO> getApplicantsForRecruiter() {
        User recruiter = getCurrentUser();

        return jobApplicationRepository
                .findByJob_Recruiter_IdOrderByAppliedAtDesc(recruiter.getId())
                .stream()
                .map(JobApplicationResponseDTO::from)
                .toList();
    }

    @Transactional
    public JobApplicationResponseDTO updateApplicationStatus(
            Long applicationId,
            ApplicationStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        User recruiter = getCurrentUser();

        JobApplication application = jobApplicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Application not found: " + applicationId));

        if (application.getJob().getRecruiter() == null
                || !application.getJob().getRecruiter().getId()
                .equals(recruiter.getId())) {
            throw new AccessDeniedException(
                    "You cannot update another recruiter's application");
        }

        application.setStatus(status);

        return JobApplicationResponseDTO.from(
                jobApplicationRepository.save(application));
    }

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Logged-in user not found"));
    }
}
