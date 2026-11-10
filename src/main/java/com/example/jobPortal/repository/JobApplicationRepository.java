
package com.example.jobPortal.repository;

import com.example.jobPortal.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    boolean existsByJob_IdAndCandidate_Id(Long jobId, Long candidateId);

    List<JobApplication> findByCandidate_IdOrderByAppliedAtDesc(Long candidateId);

    List<JobApplication> findByJob_Recruiter_IdOrderByAppliedAtDesc(Long recruiterId);
}
