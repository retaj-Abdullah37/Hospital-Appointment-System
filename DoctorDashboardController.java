package projectcode1.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import projectcode1.models.User;
import projectcode1.utils.SessionManager;
import projectcode1.utils.AlertHelper;

/**
 * AdminDashboardController - Controller for Admin Dashboard
 * Demonstrates: Polymorphism, Session Management
 *
 * @author CS313 Term Project
 */
public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Get current admin from session
        User admin = SessionManager.getInstance().getCurrentUser();

        if (admin != null) {
            welcomeLabel.setText("Welcome, " + admin.getName() + "!");
        }
    }

    /**
     * Handle Manage Users button
     */
    @FXML
    private void handleManageUsers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/ManageUsers.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Manage Users");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load Manage Users screen");
            e.printStackTrace();
        }
    }

    /**
     * Handle View All Appointments button
     */
    @FXML
    private void handleViewAllAppointments(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/ViewAllAppointments.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("All Appointments");
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
