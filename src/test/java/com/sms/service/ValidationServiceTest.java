package com.sms.service;

import com.sms.domain.Student;
import com.sms.domain.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationService.
 * Covers all field rules defined in the assignment brief.
 * Run with: mvn test
 */
class ValidationServiceTest {

    private ValidationService vs;

    @BeforeEach
    void setUp() {
        vs = new ValidationService();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Student ID tests
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test01_validStudentId_passes() {
        ValidationResult r = new ValidationResult();
        vs.validateStudentId("STU0001", r);
        assertTrue(r.isValid(), "Valid ID should pass");
    }

    @Test
    void test02_blankStudentId_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateStudentId("", r);
        assertFalse(r.isValid(), "Blank ID should fail");
    }

    @Test
    void test03_tooShortStudentId_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateStudentId("AB", r);  // only 2 chars, minimum is 4
        assertFalse(r.isValid(), "ID shorter than 4 chars should fail");
    }

    @Test
    void test04_specialCharsInStudentId_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateStudentId("STU_001!", r);
        assertFalse(r.isValid(), "ID with special characters should fail");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Full name tests
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test05_validFullName_passes() {
        ValidationResult r = new ValidationResult();
        vs.validateFullName("John Mensah", r);
        assertTrue(r.isValid(), "Valid name should pass");
    }

    @Test
    void test06_nameWithDigits_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateFullName("John123", r);
        assertFalse(r.isValid(), "Name with digits should fail");
    }

    @Test
    void test07_tooShortName_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateFullName("A", r);
        assertFalse(r.isValid(), "Name shorter than 2 chars should fail");
    }

    // ────────────────────────────────────────────────────────────────────────
    // GPA tests
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test08_gpaAtMinBoundary_passes() {
        ValidationResult r = new ValidationResult();
        vs.validateGpa(0.0, r);
        assertTrue(r.isValid(), "GPA of 0.0 should be valid");
    }

    @Test
    void test09_gpaAtMaxBoundary_passes() {
        ValidationResult r = new ValidationResult();
        vs.validateGpa(4.0, r);
        assertTrue(r.isValid(), "GPA of 4.0 should be valid");
    }

    @Test
    void test10_gpaAboveMax_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateGpa(4.1, r);
        assertFalse(r.isValid(), "GPA above 4.0 should fail");
    }

    @Test
    void test11_gpaBelowMin_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateGpa(-0.1, r);
        assertFalse(r.isValid(), "GPA below 0.0 should fail");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Level tests
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test12_validLevel_passes() {
        ValidationResult r = new ValidationResult();
        vs.validateLevel(300, r);
        assertTrue(r.isValid(), "Level 300 should be valid");
    }

    @Test
    void test13_invalidLevel_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateLevel(150, r);
        assertFalse(r.isValid(), "Level 150 is not a valid level");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Email tests
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test14_validEmail_passes() {
        ValidationResult r = new ValidationResult();
        vs.validateEmail("john@example.com", r);
        assertTrue(r.isValid(), "Valid email should pass");
    }

    @Test
    void test15_emailMissingAt_fails() {
        ValidationResult r = new ValidationResult();
        vs.validateEmail("johnexample.com", r);
        assertFalse(r.isValid(), "Email without @ should fail");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Phone tests
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test16_validPhone_passes() {
        ValidationResult r = new ValidationResult();
        vs.validatePhone("0244123456", r);
        assertTrue(r.isValid(), "10-digit phone should pass");
    }

    @Test
    void test17_phoneWithLetters_fails() {
        ValidationResult r = new ValidationResult();
        vs.validatePhone("024ABC4567", r);
        assertFalse(r.isValid(), "Phone with letters should fail");
    }

    @Test
    void test18_phoneTooShort_fails() {
        ValidationResult r = new ValidationResult();
        vs.validatePhone("024", r);
        assertFalse(r.isValid(), "Phone shorter than 10 digits should fail");
    }

    // ────────────────────────────────────────────────────────────────────────
    // Full student validation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void test19_fullValidStudent_passes() {
        Student s = new Student("STU0001", "Kwame Asante", "Computer Science",
                300, 3.5, "kwame@stu.edu.gh", "0244123456",
                LocalDate.now(), "Active");
        ValidationResult r = vs.validate(s);
        assertTrue(r.isValid(), "A complete, valid student should pass all checks");
    }

    @Test
    void test20_multipleErrors_allReported() {
        Student s = new Student("AB", "Jo3", "CS", 150, 5.0,
                "notanemail", "123", LocalDate.now(), "Active");
        ValidationResult r = vs.validate(s);
        assertFalse(r.isValid(), "Student with multiple bad fields should fail");
        assertTrue(r.getErrors().size() > 1, "Should report multiple errors at once");
    }
}
