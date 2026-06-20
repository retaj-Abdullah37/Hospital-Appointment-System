package projectcode1.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import projectcode1.dao.PatientDAO;
import projectcode1.dao.DoctorDAO;
import projectcode1.dao.DatabaseConnection;
import projectcode1.models.Patient;
import projectcode1.models.Doctor;
import projectcode1.models.User;
import projectcode1.utils.SessionManager;
import projectcode1.utils.AlertHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LoginController - Controller for Login View
 * Demonstrates: Exception Handling, Polymorphism
 *
 * Handles user authentication for all user types (Patient, Doctor, Admin)
 *
 * @author CS313 Term Project
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label statusLabel;

    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        patientDAO = new PatientDAO();
        doctorDAO = new DoctorDAO();
        statusLabel.setText("");
    }

    /**
     * Handle login button click
     * Demonstrates: Exception Handling with try-catch
     * @param event Action event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            AlertHelper.showEmptyFieldsError();
            return;
        }

        try {
            // Authenticate user
            User user = authenticateUser(username, password);

            if (user != null) {
                // Set current user in session (Polymorphism - user can be Patient, Doctor, or Admin)
                SessionManager.getInstance().setCurrentUser(user);

                statusLabel.setText("Login successful!");

                // Redirect to appropriate dashboard based on user role
                redirectToDashboard(event, user);

            } else {
                statusLabel.setText("Invalid username or password");
                AlertHelper.showLoginError();
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error occurred");
            AlertHelper.showDatabaseError(e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("An error occurred");
            AlertHelper.showError("Error", "Login Error", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle register button click
     * Opens the registration screen
     * @param event Action event
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            // Load Register view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/RegisterView.fxml"));
            Parent root = loader.load();

            // Get current stage and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Patient Registration");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load registration screen");
            e.printStackTrace();
        }
    }

    /**
     * Authenticate user by checking credentials in database
     * Demonstrates: Exception Handling, Polymorphism
     *
     * @param username Username
     * @param password Password
     * @return User object (Patient, Doctor, or Admin), or null if authentication fails
     * @throws SQLException if database error occurs
     */
    private User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT USER_ID, ROLE FROM USERS WHERE USERNAME = ? AND PASSWORD = ?";

        Connection connection = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();

        User user = null;

        if (rs.next()) {
            int userId = rs.getInt("USER_ID");
            String role = rs.getString("ROLE");

            // Polymorphism - Load appropriate user type based on role
            if ("PATIENT".equals(role)) {
                user = patientDAO.getById(userId);
            } else if ("DOCTOR".equals(role)) {
                user = doctorDAO.getById(userId);
            } else if ("ADMIN".equals(role)) {
                // For admin, we can create a simple User object or Admin object
                // For now, we'll load it as a generic user
                user = loadAdminUser(userId);
            }
        }

        rs.close();
        stmt.close();

        return user;
    }

    /**
     * Load admin user from database
     * @param userId User ID
     * @return Admin user object
     * @throws SQLException if database error occurs
     */
    private User loadAdminUser(int userId) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE USER_ID = ?";

        Connection connection = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setInt(1, userId);

        ResultSet rs = stmt.executeQuery();

        projectcode1.models.Admin admin = null;

        if (rs.next()) {
            String username = rs.getString("USERNAME");
            String password = rs.getString("PASSWORD");
            String name = rs.getString("NAME");
            String email = rs.getString("EMAIL");
            String phone = rs.getString("PHONE");

            admin = new projectcode1.models.Admin(userId, username, password,
                                                  name, email, phone, 1);
        }

        rs.close();
        stmt.close();

        return admin;
    }

    /**
     * Redirect to appropriate dashboard based on user role
     * Demonstrates: Polymorphism (instanceof operator)
     *
     * @param event Action event
     * @param user Logged-in user
     */
    private void redirectToDashboard(ActionEvent event, User user) {
        try {
            String fxmlFile = "";

            // Determine which dashboard to load (Polymorphism)
            if (user instanceof Patient) {
                fxmlFile = "/projectcode1/views/PatientDashboard.fxml";
            } else if (user instanceof Doctor) {
                fxmlFile = "/projectcode1/views/DoctorDashboard.fxml";
            } else {
                // Admin
                fxmlFile = "/projectcode1/views/AdminDashboard.fxml";
            }

            // Load the appropriate dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Get current stage and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - " + user.getName());
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error",
                                 "Could not load dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
