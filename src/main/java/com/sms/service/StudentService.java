package com.sms.service;

import com.sms.domain.Student;
import com.sms.domain.ValidationResult;
import com.sms.repository.StudentRepository;
import com.sms.util.AppLogger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles all business logic for student operations.
 * Controllers only call this class – never the repository directly.
 */
public class StudentService {

    private final StudentRepository repository;
    private final ValidationService validationService;

    public StudentService(StudentRepository repository, ValidationService validationService) {
        this.repository = repository;
        this.validationService = validationService;
    }

    // ── Add ───────────────────────────────────────────────────────────────────

    /**
     * Validates and saves a new student.
     * @throws IllegalArgumentException if validation fails or ID already exists.
     */
    public void addStudent(Student student) {
        ValidationResult result = validationService.validate(student);
        if (!result.isValid()) throw new IllegalArgumentException(result.getErrorMessage());

        if (repository.existsById(student.getStudentId())) {
            throw new IllegalArgumentException("Student ID '" + student.getStudentId() + "' already exists.");
        }
        repository.save(student);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    public void updateStudent(Student student) {
        ValidationResult result = validationService.validate(student);
        if (!result.isValid()) throw new IllegalArgumentException(result.getErrorMessage());

        if (!repository.existsById(student.getStudentId())) {
            throw new IllegalArgumentException("Student ID '" + student.getStudentId() + "' not found.");
        }
        repository.update(student);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    public void deleteStudent(String studentId) {
        if (!repository.existsById(studentId)) {
            throw new IllegalArgumentException("Student ID '" + studentId + "' not found.");
        }
        repository.delete(studentId);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    public List<Student> getAllStudents() {
        return repository.findAll();
    }

    public Optional<Student> getStudentById(String id) {
        return repository.findById(id);
    }

    public List<Student> searchStudents(String query) {
        if (query == null || query.isBlank()) return repository.findAll();
        return repository.search(query.trim());
    }

    public List<Student> filterStudents(String programme, Integer level, String status) {
        return repository.filter(programme, level, status);
    }

    public List<String> getAllProgrammes() {
        return repository.findAllProgrammes();
    }

    // ── Dashboard Stats ───────────────────────────────────────────────────────

    public long getTotalCount() {
        return repository.findAll().size();
    }

    public long getActiveCount() {
        return repository.findAll().stream().filter(s -> "Active".equals(s.getStatus())).count();
    }

    public long getInactiveCount() {
        return repository.findAll().stream().filter(s -> "Inactive".equals(s.getStatus())).count();
    }

    public double getAverageGpa() {
        List<Student> all = repository.findAll();
        if (all.isEmpty()) return 0.0;
        return all.stream().mapToDouble(Student::getGpa).average().orElse(0.0);
    }

    // ── Reports ───────────────────────────────────────────────────────────────

    /** Top N students by GPA, optionally filtered by programme and level. */
    public List<Student> getTopPerformers(int n, String programme, Integer level) {
        List<Student> students = repository.filter(programme, level, "Active");
        return students.stream()
                .sorted(Comparator.comparingDouble(Student::getGpa).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /** Students with GPA below the given threshold. */
    public List<Student> getAtRiskStudents(double threshold) {
        return repository.findAll().stream()
                .filter(s -> s.getGpa() < threshold)
                .sorted(Comparator.comparingDouble(Student::getGpa))
                .collect(Collectors.toList());
    }

    /** Count students per GPA band: 0-1, 1-2, 2-3, 3-4. */
    public Map<String, Long> getGpaDistribution() {
        List<Student> all = repository.findAll();
        Map<String, Long> dist = new LinkedHashMap<>();
        dist.put("0.0 – 1.0", all.stream().filter(s -> s.getGpa() <  1.0).count());
        dist.put("1.0 – 2.0", all.stream().filter(s -> s.getGpa() >= 1.0 && s.getGpa() < 2.0).count());
        dist.put("2.0 – 3.0", all.stream().filter(s -> s.getGpa() >= 2.0 && s.getGpa() < 3.0).count());
        dist.put("3.0 – 4.0", all.stream().filter(s -> s.getGpa() >= 3.0).count());
        return dist;
    }

    /** Per-programme: total students and average GPA. */
    public List<Map<String, Object>> getProgrammeSummary() {
        List<Student> all = repository.findAll();
        Map<String, List<Student>> grouped = all.stream()
                .collect(Collectors.groupingBy(Student::getProgramme));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Student>> entry : grouped.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            double avg = entry.getValue().stream().mapToDouble(Student::getGpa).average().orElse(0.0);
            row.put("Programme", entry.getKey());
            row.put("Total", entry.getValue().size());
            row.put("Average GPA", String.format("%.2f", avg));
            result.add(row);
        }
        result.sort(Comparator.comparing(m -> m.get("Programme").toString()));
        return result;
    }
}
