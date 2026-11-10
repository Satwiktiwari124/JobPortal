 package com.example.jobPortal.controller;

import com.example.jobPortal.entity.CandidateResume;
import com.example.jobPortal.service.ResumeService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> uploadResume(
            @RequestParam("file") MultipartFile file) {

        CandidateResume resume = resumeService.uploadResume(file);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Resume uploaded successfully",
                        "resumeId", resume.getId(),
                        "fileName", resume.getOriginalFileName(),
                        "uploadedAt", resume.getUploadedAt()
                )
        );
    }

    @GetMapping("/applications/{applicationId}/download")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Resource> downloadApplicantResume(
            @PathVariable Long applicationId) throws IOException {
        CandidateResume resume = resumeService.getResumeForApplication(applicationId);
        Path filePath = resumeService.getResumeFilePath(resume);
        Resource resource = new UrlResource(filePath.toUri());
        String safeName = resume.getOriginalFileName().replaceAll("[^a-zA-Z0-9._-]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeName + "\"")
                .body(resource);
    }

    @GetMapping("/my/download")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Resource> downloadMyResume()
            throws IOException {

        CandidateResume resume = resumeService.getMyResume();

        Path filePath = resumeService.getResumeFilePath(resume);
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.pdf\""
                )
                .body(resource);
    }
}
