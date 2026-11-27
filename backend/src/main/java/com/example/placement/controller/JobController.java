package com.example.placement.controller;

import com.example.placement.model.Job;
import com.example.placement.repository.JobRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "http://localhost:5173")
public class JobController {
    private final JobRepository repo;
    public JobController(JobRepository repo) { this.repo = repo; }
    @GetMapping public List<Job> all() { return repo.findAll(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Job create(@Valid @RequestBody Job job) { return repo.save(job); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
        try { repo.deleteById(id); }
        catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This job has applications and cannot be deleted", ex);
        }
    }
}
