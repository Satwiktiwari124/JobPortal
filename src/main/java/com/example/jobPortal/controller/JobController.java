
package com.example.jobPortal.controller;

import jakarta.validation.Valid;
import com.example.jobPortal.dto.JobRequestDTO;
import com.example.jobPortal.dto.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    public ResponseEntity<JobResponseDTO> createJob(
            @Valid @RequestBody JobRequestDTO jobRequestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.saveJob(jobRequestDTO));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponseDTO>> getAllJobs() {

        List<JobResponseDTO> jobs = jobService.getAllJobs()
                .stream()
                .map(job -> new JobResponseDTO(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getDescription(),
                        job.getRecruiter() != null
                                ? job.getRecruiter().getId() : null,
                        job.getRecruiter() != null
                                ? job.getRecruiter().getUsername() : null
                ))
                .toList();

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<JobResponseDTO> getJobById(
            @PathVariable Long id) {

        Job job = jobService.getJobById(id);

        JobResponseDTO response = new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                job.getRecruiter() != null
                        ? job.getRecruiter().getId() : null,
                job.getRecruiter() != null
                        ? job.getRecruiter().getUsername() : null
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/jobs/{id}")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody Job job) {

        Job updatedJob = jobService.updateJob(id, job);

        if (updatedJob == null) {
            return ResponseEntity.notFound().build();
        }

        JobResponseDTO response = new JobResponseDTO(
                updatedJob.getId(),
                updatedJob.getTitle(),
                updatedJob.getCompany(),
                updatedJob.getLocation(),
                updatedJob.getDescription(),
                updatedJob.getRecruiter() != null ? updatedJob.getRecruiter().getId() : null,
                updatedJob.getRecruiter() != null ? updatedJob.getRecruiter().getUsername() : null
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {

        boolean deleted = jobService.deleteJob(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
