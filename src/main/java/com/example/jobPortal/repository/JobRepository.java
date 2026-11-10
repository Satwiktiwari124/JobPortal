package com.example.jobPortal.repository;

import com.example.jobPortal.entity.Job;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobRepository extends JpaRepository<Job, Long> {
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.recruiter")
    List<Job> findAllWithRecruiter();
}