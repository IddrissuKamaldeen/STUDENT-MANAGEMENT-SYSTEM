package com.sms.ui.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Controls the main window. Swaps the center content when navigation buttons are clicked.
 */
public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        showDashboard(); // default screen on startup
    }

    // ────────────────────────────────────────────────
    // Navigation handler methods (called from FXML)
    // ────────────────────────────────────────────────

    @FXML
    private void showDashboard() {
        loadView("DashboardView.fxml");
    }

    @FXML
    private void showStudents() {
        loadView("StudentsView.fxml");
    }

    @FXML
    private void showReports() {
        loadView("ReportsView.fxml");
    }

    @FXML
    private void showImportExport() {
        loadView("ImportExportView.fxml");
    }

    @FXML
    private void showSettings() {
        loadView("SettingsView.fxml");
    }

    // ────────────────────────────────────────────────
    // Core view loading logic
    // ────────────────────────────────────────────────

    private void loadView(String fxmlFileName) {
        try {
            URL fxmlUrl = getClass().getResource("/com/sms/ui/" + fxmlFileName);
            if (fxmlUrl == null) {
                LOGGER.severe(() -> "FXML file not found: " + fxmlFileName);
                // Optional: show error message in UI here
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node rootNode = loader.load();

            // Safest and clearest way — avoids any setAll() ambiguity
            contentArea.getChildren().clear();
            contentArea.getChildren().add(rootNode);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load view: " + fxmlFileName, e);
            // Optional: show user-friendly error dialog here
            // e.g. showErrorDialog("Cannot load page", e.getMessage());
        }
    }
}