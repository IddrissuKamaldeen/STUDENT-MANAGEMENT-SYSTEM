package com.sms.service;

import com.sms.domain.Student;
import com.sms.domain.ValidationResult;
import com.sms.util.AppLogger;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Handles all CSV import and export operations.
 * All exported files go to the 'data' folder.
 */
public class CsvService {

    private static final String DATA_DIR = "data";
    private final ValidationService validationService = new ValidationService();

    // ── Export ────────────────────────────────────────────────────────────────

    public void exportStudents(List<Student> students, String filename) throws IOException {
        File file = new File(DATA_DIR + File.separator + filename);
        new File(DATA_DIR).mkdirs();

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("student_id,full_name,programme,level,gpa,email,phone_number,date_added,status");
            for (Student s : students) {
                pw.println(toCsvRow(s));
            }
        }
        AppLogger.info("Export complete: " + file.getAbsolutePath() + " (" + students.size() + " records)");
    }

    // ── Import ────────────────────────────────────────────────────────────────

    /**
     * Parses a CSV file and returns an ImportResult with valid students and error lines.
     * Invalid rows are skipped and logged – the app will never crash on bad data.
     */
    public ImportResult importFromCsv(File file, Set<String> existingIds) throws IOException {
        List<Student> valid = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (firstLine) { firstLine = false; continue; } // skip header
                if (line.isBlank()) continue;

                try {
                    Student s = parseLine(line, lineNumber);

                    // Duplicate check
                    if (existingIds.contains(s.getStudentId())) {
                        errors.add("Line " + lineNumber + ": Duplicate ID '" + s.getStudentId() + "' – skipped.");
                        continue;
                    }

                    // Validation check
                    ValidationResult vr = validationService.validate(s);
                    if (!vr.isValid()) {
                        errors.add("Line " + lineNumber + ": " + vr.getErrorMessage());
                        continue;
                    }

                    existingIds.add(s.getStudentId()); // track within-file duplicates
                    valid.add(s);

                } catch (Exception e) {
                    errors.add("Line " + lineNumber + ": Could not parse row – " + e.getMessage());
                }
            }
        }

        AppLogger.info("Import complete: " + valid.size() + " imported, " + errors.size() + " errors.");
        return new ImportResult(valid, errors);
    }

    /** Saves the import error list to data/import_errors.csv */
    public void saveImportErrorReport(List<String> errors) throws IOException {
        new File(DATA_DIR).mkdirs();
        File file = new File(DATA_DIR + File.separator + "import_errors.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("error_description");
            for (String err : errors) {
                pw.println("\"" + err.replace("\"", "\"\"") + "\"");
            }
        }
        AppLogger.info("Import error report saved: " + errors.size() + " errors.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Student parseLine(String line, int lineNum) {
        String[] parts = line.split(",", -1);
        if (parts.length < 9) throw new IllegalArgumentException("Expected 9 columns, found " + parts.length);

        Student s = new Student();
        s.setStudentId(parts[0].trim());
        s.setFullName(parts[1].trim());
        s.setProgramme(parts[2].trim());
        s.setLevel(Integer.parseInt(parts[3].trim()));
        s.setGpa(Double.parseDouble(parts[4].trim()));
        s.setEmail(parts[5].trim());
        s.setPhoneNumber(parts[6].trim());
        s.setDateAdded(LocalDate.parse(parts[7].trim()));
        s.setStatus(parts[8].trim());
        return s;
    }

    private String toCsvRow(Student s) {
        return String.join(",",
                s.getStudentId(),
                escape(s.getFullName()),
                escape(s.getProgramme()),
                String.valueOf(s.getLevel()),
                String.valueOf(s.getGpa()),
                s.getEmail(),
                s.getPhoneNumber(),
                s.getDateAdded().toString(),
                s.getStatus()
        );
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ── ImportResult inner class ──────────────────────────────────────────────

    public static class ImportResult {
        public final List<Student> validStudents;
        public final List<String> errors;

        public ImportResult(List<Student> validStudents, List<String> errors) {
            this.validStudents = validStudents;
            this.errors = errors;
        }
    }
}
