 package com.example.jobPortal.service;

import com.example.jobPortal.entity.CandidateResume;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.repository.CandidateResumeRepository;
import com.example.jobPortal.repository.JobApplicationRepository;
import com.example.jobPortal.entity.JobApplication;
import org.springframework.security.access.AccessDeniedException;
import com.example.jobPortal.repository.UserRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ResumeService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final CandidateResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final Path uploadDirectory;

    public ResumeService(
            CandidateResumeRepository resumeRepository,
            UserRepository userRepository,
            JobApplicationRepository jobApplicationRepository,
            @Value("${app.upload.dir:uploads/resumes}") String uploadDir) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public CandidateResume uploadResume(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a PDF resume");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Resume size must not exceed 5 MB");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null
                || !originalName.toLowerCase()
                .endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Only PDF files are allowed");
        }

        byte[] bytes;

        try {
            bytes = file.getBytes();

            if (bytes.length < 5
                    || bytes[0] != '%'
                    || bytes[1] != 'P'
                    || bytes[2] != 'D'
                    || bytes[3] != 'F'
                    || bytes[4] != '-') {
                throw new IllegalArgumentException(
                        "The uploaded file is not a valid PDF");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unable to read uploaded resume", e);
        }

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User candidate = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("User not found"));

        String storedName = UUID.randomUUID() + ".pdf";
        Path target = uploadDirectory.resolve(storedName).normalize();

        if (!target.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        String extractedText;

        try (var document = Loader.loadPDF(bytes)) {

            extractedText = new PDFTextStripper().getText(document);

            if (extractedText == null || extractedText.isBlank()) {
                throw new IllegalArgumentException(
                        "No readable text found in PDF. "
                                + "Please upload a text-based PDF");
            }

            Files.createDirectories(uploadDirectory);
            Files.write(target, bytes);

        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read PDF. "
                            + "Please upload an uncorrupted PDF", e);
        }

        CandidateResume resume = resumeRepository
                .findByCandidate_Id(candidate.getId())
                .orElseGet(CandidateResume::new);

        resume.setCandidate(candidate);
        resume.setStoredFileName(storedName);
        resume.setOriginalFileName(
                Paths.get(originalName).getFileName().toString());
        resume.setExtractedText(extractedText);

        return resumeRepository.save(resume);
    }

    @Transactional(readOnly = true)
    public CandidateResume getMyResume() {

        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User candidate = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException("User not found"));

        return resumeRepository.findByCandidate_Id(candidate.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Please upload your resume first"));
    }

    @Transactional(readOnly = true)
    public CandidateResume getResumeForApplication(Long applicationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User recruiter = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Recruiter not found"));
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));
        if (application.getJob().getRecruiter() == null
                || !application.getJob().getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("You can view resumes only for your own job applicants");
        }
        return resumeRepository.findByCandidate_Id(application.getCandidate().getId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate has not uploaded a resume"));
    }

    public Path getResumeFilePath(CandidateResume resume) {

        Path filePath = uploadDirectory
                .resolve(resume.getStoredFileName())
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Invalid file path");
        }

        if (!Files.isRegularFile(filePath)) {
            throw new IllegalArgumentException(
                    "Resume file not found on server");
        }

        return filePath;
    }
}
