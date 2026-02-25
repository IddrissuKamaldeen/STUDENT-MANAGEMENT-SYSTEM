package com.sms.ui.controllers;

import com.sms.domain.Student;
import com.sms.service.CsvService;
import com.sms.service.StudentService;
import com.sms.util.AppLogger;
import com.sms.util.ServiceLocator;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controls the Import / Export screen.
 * Import runs on a background thread so the UI does not freeze.
 */
public class ImportExportController {

    @FXML private Label    lblImportFile;
    @FXML private VBox     importResultBox;
    @FXML private Label    lblImportSuccess;
    @FXML private Label    lblImportErrors;
    @FXML private TextArea txtImportLog;
    @FXML private Label    lblExportStatus;

    private final StudentService studentService = ServiceLocator.getStudentService();
    private final CsvService     csvService     = ServiceLocator.getCsvService();
    private File selectedImportFile;
    private List<String> lastImportErrors;

    // ── Import ────────────────────────────────────────────────────────────────

    @FXML
    public void chooseImportFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select CSV File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(lblImportFile.getScene().getWindow());
        if (file != null) {
            selectedImportFile = file;
            lblImportFile.setText(file.getName());
        }
    }

    @FXML
    public void runImport() {
        if (selectedImportFile == null) {
            showAlert(Alert.AlertType.WARNING, "No File", "Please choose a CSV file first.");
            return;
        }

        txtImportLog.setText("Importing, please wait…");
        importResultBox.setVisible(false);
        importResultBox.setManaged(false);

        // Run on background thread to keep UI responsive
        Task<CsvService.ImportResult> task = new Task<>() {
            @Override
            protected CsvService.ImportResult call() throws Exception {
                Set<String> existingIds = studentService.getAllStudents()
                        .stream().map(Student::getStudentId).collect(Collectors.toSet());
                return csvService.importFromCsv(selectedImportFile, existingIds);
            }
        };

        task.setOnSucceeded(e -> {
            CsvService.ImportResult result = task.getValue();
            lastImportErrors = result.errors;

            // Save valid records
            int saved = 0;
            StringBuilder log = new StringBuilder();
            for (Student s : result.validStudents) {
                try {
                    studentService.addStudent(s);
                    saved++;
                } catch (Exception ex) {
                    log.append("Could not save ").append(s.getStudentId()).append(": ").append(ex.getMessage()).append("\n");
                }
            }

            for (String err : result.errors) log.append(err).append("\n");

            lblImportSuccess.setText("✅ " + saved + " student(s) imported successfully.");
            lblImportErrors.setText("⚠️ " + result.errors.size() + " row(s) skipped with errors.");
            txtImportLog.setText(log.toString().isEmpty() ? "No errors." : log.toString());
            importResultBox.setVisible(true);
            importResultBox.setManaged(true);
        });

        task.setOnFailed(e -> {
            txtImportLog.setText("Import failed: " + task.getException().getMessage());
            AppLogger.error("Import failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    public void saveErrorReport() {
        if (lastImportErrors == null || lastImportErrors.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "No Errors", "There are no import errors to save.");
            return;
        }
        try {
            csvService.saveImportErrorReport(lastImportErrors);
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Error report saved to data/import_errors.csv");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not save error report: " + e.getMessage());
        }
    }

    // ── Export ────────────────────────────────────────────────────────────────

    @FXML
    public void exportAll() {
        try {
            List<Student> all = studentService.getAllStudents();
            csvService.exportStudents(all, "all_students.csv");
            lblExportStatus.setText("✅ Exported all_students.csv to the data folder (" + all.size() + " records).");
        } catch (IOException e) {
            lblExportStatus.setText("❌ Export failed: " + e.getMessage());
        }
    }

    @FXML
    public void exportTopPerformers() {
        try {
            List<Student> top = studentService.getTopPerformers(10, null, null);
            csvService.exportStudents(top, "top_performers.csv");
            lblExportStatus.setText("✅ Exported top_performers.csv (" + top.size() + " records).");
        } catch (IOException e) {
            lblExportStatus.setText("❌ Export failed: " + e.getMessage());
        }
    }

    @FXML
    public void exportAtRisk() {
        try {
            List<Student> risk = studentService.getAtRiskStudents(ServiceLocator.getAtRiskThreshold());
            csvService.exportStudents(risk, "at_risk_students.csv");
            lblExportStatus.setText("✅ Exported at_risk_students.csv (" + risk.size() + " records).");
        } catch (IOException e) {
            lblExportStatus.setText("❌ Export failed: " + e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.showAndWait();
    }
}
