package com.sms.ui.controllers;

import com.sms.util.AppLogger;
import com.sms.util.ServiceLocator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controls the Settings screen. Allows changing the at-risk GPA threshold.
 */
public class SettingsController {

    @FXML private TextField txtThreshold;
    @FXML private Label     lblSettingsMsg;

    @FXML
    public void initialize() {
        txtThreshold.setText(String.valueOf(ServiceLocator.getAtRiskThreshold()));
    }

    @FXML
    public void saveThreshold() {
        try {
            double value = Double.parseDouble(txtThreshold.getText().trim());
            if (value < 0.0 || value > 4.0) {
                lblSettingsMsg.setText("⚠️ Threshold must be between 0.0 and 4.0.");
                lblSettingsMsg.setStyle("-fx-text-fill: red;");
                return;
            }
            ServiceLocator.setAtRiskThreshold(value);
            lblSettingsMsg.setText("✅ Threshold updated to " + value);
            lblSettingsMsg.setStyle("-fx-text-fill: green;");
            AppLogger.info("At-risk threshold changed to: " + value);
        } catch (NumberFormatException e) {
            lblSettingsMsg.setText("⚠️ Please enter a valid number.");
            lblSettingsMsg.setStyle("-fx-text-fill: red;");
        }
    }
}
