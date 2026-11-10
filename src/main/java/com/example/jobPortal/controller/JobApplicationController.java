
package com.example.jobPortal.controller;

import com.example.jobPortal.dto.JobApplicationResponseDTO;
import com.example.jobPortal.entity.ApplicationStatus;
import com.example.jobPortal.service.JobApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(
            JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping("/jobs/{jobId}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public JobApplicationResponseDTO applyForJob(
            @PathVariable Long jobId) {
        return jobApplicationService.applyForJob(jobId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public List<JobApplicationResponseDTO> getMyApplications() {
        return jobApplicationService.getMyApplications();
    }

    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<JobApplicationResponseDTO> getApplicantsForRecruiter() {
        return jobApplicationService.getApplicantsForRecruiter();
    }

    @PatchMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public JobApplicationResponseDTO updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody StatusRequest request) {
        return jobApplicationService.updateApplicationStatus(
                applicationId,
                request.getStatus());
    }

    public static class StatusRequest {

        @NotNull(message = "Status is required")
        private ApplicationStatus status;

        public ApplicationStatus getStatus() {
            return status;
        }

        public void setStatus(ApplicationStatus status) {
            this.status = status;
        }
    }
}
