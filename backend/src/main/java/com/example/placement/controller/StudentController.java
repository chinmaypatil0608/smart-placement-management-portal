package com.example.placement.controller;

import com.example.placement.model.Student;
import com.example.placement.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {
    private final StudentRepository repo;
    public StudentController(StudentRepository repo) { this.repo = repo; }

    @GetMapping @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") public List<Student> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public Student one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student create(@Valid @RequestBody Student student) {
        if (repo.existsByEmailIgnoreCase(student.getEmail().trim()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A student with this email already exists");
        student.setEmail(student.getEmail().trim().toLowerCase());
        return repo.save(student);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody Student details) {
        Student student = one(id);
        String email = details.getEmail().trim().toLowerCase();
        if (!student.getEmail().equalsIgnoreCase(email) && repo.existsByEmailIgnoreCase(email))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already used by another student");
        student.setName(details.getName().trim());
        student.setEmail(email);
        student.setDepartment(details.getDepartment().trim());
        student.setCgpa(details.getCgpa());
        return repo.save(student);
    }
}
