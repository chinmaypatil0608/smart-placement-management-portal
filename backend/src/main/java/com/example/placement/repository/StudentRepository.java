package com.example.placement.repository;

import com.example.placement.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmailIgnoreCase(String email);
    java.util.Optional<Student> findByEmailIgnoreCase(String email);
}
