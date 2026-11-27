package com.example.placement.controller;

import com.example.placement.model.*;
import com.example.placement.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class ApplicationController {
    private static final List<String> STATUSES = List.of("APPLIED", "ASSESSMENT", "INTERVIEW", "SELECTED", "REJECTED");
    private final JobApplicationRepository apps;
    private final StudentRepository students;
    private final JobRepository jobs;

    public ApplicationController(JobApplicationRepository apps, StudentRepository students, JobRepository jobs) {
        this.apps = apps; this.students = students; this.jobs = jobs;
    }

    @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") public List<JobApplication> all() { return apps.findAll(); }

    @GetMapping("/student/{studentId}")
    public List<JobApplication> byStudent(@PathVariable Long studentId, java.security.Principal principal) {
        Student own = students.findByEmailIgnoreCase(principal.getName()).orElse(null);
        boolean admin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && (own == null || !own.getId().equals(studentId))) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only view your own applications");
        if (!students.existsById(studentId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        return apps.findByStudentId(studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobApplication apply(@RequestParam Long studentId, @RequestParam Long jobId, java.security.Principal principal) {
        Student own = students.findByEmailIgnoreCase(principal.getName()).orElse(null);
        if (own == null || !own.getId().equals(studentId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only apply using your own student profile");
        Student student = students.findById(studentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
        Job job = jobs.findById(jobId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        if (student.getCgpa() < job.getMinimumCgpa())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum CGPA criteria not met");
        if (apps.existsByStudentIdAndJobId(studentId, jobId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already applied for this job");
        return apps.save(new JobApplication(student, job));
    }

    @PatchMapping("/{id}/status")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public JobApplication status(@PathVariable Long id, @RequestParam String status) {
        JobApplication application = apps.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        String value = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(value)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status. Allowed: " + STATUSES);
        application.setStatus(value);
        return apps.save(application);
    }
}
