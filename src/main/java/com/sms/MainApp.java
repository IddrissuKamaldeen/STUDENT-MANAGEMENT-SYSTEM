package com.sms;

import com.sms.util.AppLogger;
import com.sms.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Student Management System Plus.
 * Launches JavaFX, loads the main window, and handles shutdown cleanly.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppLogger.info("Application started.");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sms/ui/MainView.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 700);

        primaryStage.setTitle("Student Management System Plus v1.0");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Clean shutdown
        primaryStage.setOnCloseRequest(e -> {
            AppLogger.info("Application closed.");
            DatabaseManager.closeConnection();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
