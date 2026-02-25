package com.sms.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sms.service.StudentService;
import com.sms.util.ServiceLocator;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Populates the dashboard stat cards and handles quick-action navigation buttons.
 */
public class DashboardController {

    private static final Logger LOGGER = Logger.getLogger(DashboardController.class.getName());

    @FXML private Label lblTotal;
    @FXML private Label lblActive;
    @FXML private Label lblInactive;
    @FXML private Label lblAvgGpa;

    private final StudentService studentService = ServiceLocator.getStudentService();

    @FXML
    public void initialize() {
        refreshStats();
    }

    private void refreshStats() {
        try {
            lblTotal.setText(String.valueOf(studentService.getTotalCount()));
            lblActive.setText(String.valueOf(studentService.getActiveCount()));
            lblInactive.setText(String.valueOf(studentService.getInactiveCount()));
            lblAvgGpa.setText(String.format("%.2f", studentService.getAverageGpa()));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh dashboard statistics", e);
            // Optional: set fallback text, e.g.
            // lblTotal.setText("—"); lblActive.setText("—"); etc.
        }
    }

    // ────────────────────────────────────────────────
    // Quick-action navigation handlers (called from FXML)
    // ────────────────────────────────────────────────

    @FXML
    private void goStudents() {
        navigateTo("StudentsView.fxml");
    }

    @FXML
    private void goReports() {
        navigateTo("ReportsView.fxml");
    }

    @FXML
    private void goImportExport() {
        navigateTo("ImportExportView.fxml");
    }

    @FXML
    private void goSettings() {
        navigateTo("SettingsView.fxml");
    }

    // ────────────────────────────────────────────────
    // Navigation logic – finds the main content area and swaps view
    // ────────────────────────────────────────────────

    private void navigateTo(String fxmlFileName) {
        try {
            // Lookup the content StackPane from the current scene
            // (assumes it has fx:id="contentArea" in the main FXML)
            StackPane contentArea = (StackPane) lblTotal.getScene().lookup("#contentArea");

            if (contentArea == null) {
                LOGGER.severe("Cannot find #contentArea StackPane in the scene");
                return;
            }

            URL fxmlUrl = getClass().getResource("/com/sms/ui/" + fxmlFileName);
            if (fxmlUrl == null) {
                LOGGER.severe("FXML file not found: " + fxmlFileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node newContent = loader.load();

            // Safest way to replace content (avoids setAll ambiguity)
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlFileName, e);
            // Optional: show error message in UI, e.g. temporary label in dashboard
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during navigation", e);
        }
    }

    /**
     * Public method to trigger stats refresh from outside (e.g. after student CRUD operations).
     */
    public void refresh() {
        refreshStats();
    }
}