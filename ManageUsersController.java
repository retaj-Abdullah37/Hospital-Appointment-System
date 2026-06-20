package projectcode1.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projectcode1.models.Doctor;
import projectcode1.utils.SessionManager;
import projectcode1.utils.AlertHelper;

/**
 * DoctorDashboardController - Controller for Doctor Dashboard
 * Demonstrates: Polymorphism, Session Management
 *
 * @author CS313 Term Project
 */
public class DoctorDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label specialtyLabel;

    @FXML
    private Label experienceLabel;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Get current doctor from session (Polymorphism)
        Doctor doctor = SessionManager.getInstance().getCurrentDoctor();

        if (doctor != null) {
            welcomeLabel.setText("Welcome, Dr. " + doctor.getName() + "!");
            specialtyLabel.setText("Specialty: " + doctor.getSpecialty());
            experienceLabel.setText("Experience: " + doctor.getExperienceYears() + " years");
        }
    }

    /**
     * Handle View Schedule button
     */
    @FXML
    private void handleViewSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/DoctorSchedule.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("My Schedule");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load schedule screen");
            e.printStackTrace();
        }
    }

    /**
     * Handle Logout button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Clear session
            SessionManager.getInstance().logout();

            // Return to login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hospital Appointment System - Login");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not logout");
            e.printStackTrace();
        }
    }
}
