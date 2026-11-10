
package com.example.jobPortal.repository;

import com.example.jobPortal.entity.CandidateResume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CandidateResumeRepository
        extends JpaRepository<CandidateResume, Long> {

    Optional<CandidateResume> findByCandidate_Id(Long candidateId);

    boolean existsByCandidate_Id(Long candidateId);
}
