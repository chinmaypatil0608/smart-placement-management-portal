package com.example.placement.repository;

import com.example.placement.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByStudentId(Long studentId);
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
}
