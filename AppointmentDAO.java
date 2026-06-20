package projectcode1.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projectcode1.models.Patient;
import projectcode1.utils.SessionManager;
import projectcode1.utils.AlertHelper;

/**
 * PatientDashboardController - Controller for Patient Dashboard
 * Demonstrates: Polymorphism, Session Management
 *
 * @author CS313 Term Project
 */
public class PatientDashboardController {

    @FXML
    private Label welcomeLabel;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Get current patient from session (Polymorphism)
        Patient patient = SessionManager.getInstance().getCurrentPatient();

        if (patient != null) {
            welcomeLabel.setText("Welcome, " + patient.getName() + "!");
        }
    }

    /**
     * Handle Browse Doctors button
     */
    @FXML
    private void handleBrowseDoctors(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/BrowseDoctors.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Browse Doctors");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load Browse Doctors screen");
            e.printStackTrace();
        }
    }

    /**
     * Handle My Appointments button
     */
    @FXML
    private void handleMyAppointments(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/PatientAppointments.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("My Appointments");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load Appointments screen");
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
