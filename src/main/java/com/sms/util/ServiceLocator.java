package com.sms.util;

import com.sms.repository.SQLiteStudentRepository;
import com.sms.repository.StudentRepository;
import com.sms.service.CsvService;
import com.sms.service.StudentService;
import com.sms.service.ValidationService;

/**
 * Provides shared service instances to all controllers.
 * This avoids creating duplicate service objects everywhere.
 * Think of it as a simple dependency injection container.
 */
public class ServiceLocator {

    private static StudentService studentService;
    private static CsvService csvService;
    private static double atRiskThreshold = 2.0;

    private ServiceLocator() {}

    public static StudentService getStudentService() {
        if (studentService == null) {
            StudentRepository repo = new SQLiteStudentRepository();
            ValidationService vs   = new ValidationService();
            studentService = new StudentService(repo, vs);
        }
        return studentService;
    }

    public static CsvService getCsvService() {
        if (csvService == null) {
            csvService = new CsvService();
        }
        return csvService;
    }

    public static double getAtRiskThreshold() {
        return atRiskThreshold;
    }

    public static void setAtRiskThreshold(double threshold) {
        atRiskThreshold = threshold;
    }
}
