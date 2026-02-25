package com.sms.service;

import com.sms.domain.Student;
import com.sms.domain.ValidationResult;

import java.util.Set;

/**
 * Contains all field validation rules as defined in the assignment brief.
 * Called by StudentService before any save or update.
 */
public class ValidationService {

    private static final Set<Integer> VALID_LEVELS = Set.of(100, 200, 300, 400, 500, 600, 700);

    /**
     * Validates all fields of the student object.
     * Returns a ValidationResult; check isValid() before proceeding.
     */
    public ValidationResult validate(Student student) {
        ValidationResult result = new ValidationResult();

        validateStudentId(student.getStudentId(), result);
        validateFullName(student.getFullName(), result);
        validateProgramme(student.getProgramme(), result);
        validateLevel(student.getLevel(), result);
        validateGpa(student.getGpa(), result);
        validateEmail(student.getEmail(), result);
        validatePhone(student.getPhoneNumber(), result);

        return result;
    }

    // ── Individual field validators ───────────────────────────────────────────

    public void validateStudentId(String id, ValidationResult result) {
        if (id == null || id.isBlank()) {
            result.addError("Student ID is required.");
            return;
        }
        if (id.length() < 4 || id.length() > 20) {
            result.addError("Student ID must be between 4 and 20 characters.");
        }
        if (!id.matches("[A-Za-z0-9]+")) {
            result.addError("Student ID must contain only letters and digits.");
        }
    }

    public void validateFullName(String name, ValidationResult result) {
        if (name == null || name.isBlank()) {
            result.addError("Full name is required.");
            return;
        }
        if (name.length() < 2 || name.length() > 60) {
            result.addError("Full name must be between 2 and 60 characters.");
        }
        if (name.matches(".*\\d.*")) {
            result.addError("Full name must not contain digits.");
        }
    }

    public void validateProgramme(String programme, ValidationResult result) {
        if (programme == null || programme.isBlank()) {
            result.addError("Programme is required.");
        }
    }

    public void validateLevel(int level, ValidationResult result) {
        if (!VALID_LEVELS.contains(level)) {
            result.addError("Level must be one of: 100, 200, 300, 400, 500, 600, 700.");
        }
    }

    public void validateGpa(double gpa, ValidationResult result) {
        if (gpa < 0.0 || gpa > 4.0) {
            result.addError("GPA must be between 0.0 and 4.0.");
        }
    }

    public void validateEmail(String email, ValidationResult result) {
        if (email == null || email.isBlank()) {
            result.addError("Email is required.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            result.addError("Email must contain '@' and '.'.");
        }
    }

    public void validatePhone(String phone, ValidationResult result) {
        if (phone == null || phone.isBlank()) {
            result.addError("Phone number is required.");
            return;
        }
        if (!phone.matches("\\d+")) {
            result.addError("Phone number must contain digits only.");
        }
        if (phone.length() < 10 || phone.length() > 15) {
            result.addError("Phone number must be between 10 and 15 digits.");
        }
    }
}
