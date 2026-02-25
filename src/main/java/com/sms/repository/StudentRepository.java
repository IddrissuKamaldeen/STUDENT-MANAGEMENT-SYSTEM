package com.sms.repository;

import com.sms.domain.Student;

import java.util.List;
import java.util.Optional;

/**
 * Defines all database operations for students.
 * The UI and service layers only talk to this interface,
 * never to the SQLite implementation directly.
 */
public interface StudentRepository {

    /** Save a brand new student to the database. */
    void save(Student student);

    /** Replace an existing student's data (matched by student ID). */
    void update(Student student);

    /** Remove a student by their ID. */
    void delete(String studentId);

    /** Find one student by ID. Returns empty if not found. */
    Optional<Student> findById(String studentId);

    /** Return every student in the database. */
    List<Student> findAll();

    /** Search by ID or name (case-insensitive partial match). */
    List<Student> search(String query);

    /** Filter by any combination of programme, level, status (null = ignore that filter). */
    List<Student> filter(String programme, Integer level, String status);

    /** Return all distinct programme names (for filter dropdowns). */
    List<String> findAllProgrammes();

    /** Check whether a student ID already exists. */
    boolean existsById(String studentId);
}
