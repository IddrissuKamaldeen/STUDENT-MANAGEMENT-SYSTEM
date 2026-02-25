package com.sms.ui.controllers;

import com.sms.domain.Student;
import com.sms.service.CsvService;
import com.sms.service.StudentService;
import com.sms.util.AppLogger;
import com.sms.util.ServiceLocator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controls the Reports screen. Populates all four report tables.
 */
public class ReportsController {

    // ── Top Performers ────────────────────────────────────────────────────────
    @FXML private TableView<Student> tblTop;
    @FXML private TableColumn<Student, String> topColId, topColName, topColProg, topColLvl, topColGpa;
    @FXML private ComboBox<String>  topProgramme;
    @FXML private ComboBox<Integer> topLevel;

    // ── At-Risk ───────────────────────────────────────────────────────────────
    @FXML private TableView<Student> tblRisk;
    @FXML private TableColumn<Student, String> riskColId, riskColName, riskColProg, riskColGpa, riskColStat;
    @FXML private TextField txtThreshold;

    // ── GPA Distribution ──────────────────────────────────────────────────────
    @FXML private TableView<Map.Entry<String, Long>> tblDist;
    @FXML private TableColumn<Map.Entry<String, Long>, String> distColBand, distColCount;

    // ── Programme Summary ─────────────────────────────────────────────────────
    @FXML private TableView<Map<String, Object>> tblProg;
    @FXML private TableColumn<Map<String, Object>, String> progColName, progColTotal, progColAvg;

    private final StudentService studentService = ServiceLocator.getStudentService();
    private final CsvService     csvService     = ServiceLocator.getCsvService();

    @FXML
    public void initialize() {
        setupColumns();
        // Populate filter dropdowns
        topProgramme.setItems(FXCollections.observableArrayList(studentService.getAllProgrammes()));
        topLevel.setItems(FXCollections.observableArrayList(100, 200, 300, 400, 500, 600, 700));
        txtThreshold.setText(String.valueOf(ServiceLocator.getAtRiskThreshold()));

        refreshTopPerformers();
        refreshAtRisk();
        refreshDistribution();
        refreshProgrammeSummary();
    }

    private void setupColumns() {
        // Top performers
        topColId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        topColName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        topColProg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProgramme()));
        topColLvl.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getLevel())));
        topColGpa.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getGpa())));

        // At-risk
        riskColId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentId()));
        riskColName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        riskColProg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProgramme()));
        riskColGpa.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getGpa())));
        riskColStat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // GPA distribution
        distColBand.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));
        distColCount.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getValue())));

        // Programme summary
        progColName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get("Programme").toString()));
        progColTotal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get("Total").toString()));
        progColAvg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get("Average GPA").toString()));
    }

    // ── Refresh methods ───────────────────────────────────────────────────────

    @FXML
    public void refreshTopPerformers() {
        String prog = topProgramme.getValue();
        Integer lvl = topLevel.getValue();
        List<Student> top = studentService.getTopPerformers(10, prog, lvl);
        tblTop.setItems(FXCollections.observableArrayList(top));
    }

    @FXML
    public void refreshAtRisk() {
        double threshold = parseThreshold();
        List<Student> risk = studentService.getAtRiskStudents(threshold);
        tblRisk.setItems(FXCollections.observableArrayList(risk));
    }

    @FXML
    public void refreshDistribution() {
        Map<String, Long> dist = studentService.getGpaDistribution();
        tblDist.setItems(FXCollections.observableArrayList(dist.entrySet()));
    }

    @FXML
    public void refreshProgrammeSummary() {
        List<Map<String, Object>> summary = studentService.getProgrammeSummary();
        tblProg.setItems(FXCollections.observableArrayList(summary));
    }

    // ── Export methods ────────────────────────────────────────────────────────

    @FXML
    public void exportTopPerformers() {
        try {
            String prog = topProgramme.getValue();
            Integer lvl = topLevel.getValue();
            List<Student> top = studentService.getTopPerformers(10, prog, lvl);
            csvService.exportStudents(top, "top_performers.csv");
            showInfo("Exported top_performers.csv to the data folder.");
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    @FXML
    public void exportAtRisk() {
        try {
            List<Student> risk = studentService.getAtRiskStudents(parseThreshold());
            csvService.exportStudents(risk, "at_risk_students.csv");
            showInfo("Exported at_risk_students.csv to the data folder.");
        } catch (IOException e) {
            showError("Export failed: " + e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double parseThreshold() {
        try {
            return Double.parseDouble(txtThreshold.getText().trim());
        } catch (NumberFormatException e) {
            return 2.0;
        }
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
        AppLogger.info(msg);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
        AppLogger.error(msg);
    }
}
