
package com.example.jobPortal.dto;

import com.example.jobPortal.entity.ApplicationStatus;
import com.example.jobPortal.entity.JobApplication;

import java.time.LocalDateTime;

public class JobApplicationResponseDTO {

    private Long applicationId;
    private Long jobId;
    private String jobTitle;
    private String company;
    private String location;
    private String candidateUsername;
    private String candidateEmail;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    public JobApplicationResponseDTO() {
    }

    public static JobApplicationResponseDTO from(
            JobApplication application) {

        JobApplicationResponseDTO dto =
                new JobApplicationResponseDTO();

        dto.setApplicationId(application.getId());
        dto.setJobId(application.getJob().getId());
        dto.setJobTitle(application.getJob().getTitle());
        dto.setCompany(application.getJob().getCompany());
        dto.setLocation(application.getJob().getLocation());
        dto.setCandidateUsername(
                application.getCandidate().getUsername());
        dto.setCandidateEmail(
                application.getCandidate().getEmail());
        dto.setStatus(application.getStatus());
        dto.setAppliedAt(application.getAppliedAt());

        return dto;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCandidateUsername() {
        return candidateUsername;
    }

    public void setCandidateUsername(String candidateUsername) {
        this.candidateUsername = candidateUsername;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
