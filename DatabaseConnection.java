package projectcode1.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import projectcode1.dao.DatabaseConnection;
import projectcode1.utils.AlertHelper;

import java.sql.*;
import java.time.LocalDate;

/**
 * RegisterController - Controller for Patient Registration View
 * Demonstrates: Exception Handling, Direct SQL usage
 *
 * Handles patient registration using direct SQL queries
 *
 * @author CS313 Term Project
 */
public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private ComboBox<String> bloodGroupCombo;

    @FXML
    private Label statusLabel;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        statusLabel.setText("");

        // Populate blood group combo box
        bloodGroupCombo.getItems().addAll(
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        );
    }

    /**
     * Handle register button click
     * Demonstrates: Exception Handling with try-catch
     * @param event Action event
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        LocalDate dob = dobPicker.getValue();
        String bloodGroup = bloodGroupCombo.getValue();

        // Validation
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() ||
            email.isEmpty() || phone.isEmpty()) {
            statusLabel.setText("Please fill in all required fields");
            AlertHelper.showEmptyFieldsError();
            return;
        }

        if (dob == null) {
            statusLabel.setText("Please select date of birth");
            AlertHelper.showValidationError("Please select your date of birth");
            return;
        }

        if (bloodGroup == null) {
            statusLabel.setText("Please select blood group");
            AlertHelper.showValidationError("Please select your blood group");
            return;
        }

        // Password length validation
        if (password.length() < 6) {
            statusLabel.setText("Password must be at least 6 characters");
            AlertHelper.showValidationError("Password must be at least 6 characters long");
            return;
        }

        try {
            // Check if username already exists
            if (usernameExists(username)) {
                statusLabel.setText("Username already exists");
                AlertHelper.showWarning("Registration Failed", "Username Taken",
                    "This username is already in use. Please choose another.");
                return;
            }

            // Register the patient
            registerPatient(username, password, name, email, phone, dob, bloodGroup);

            statusLabel.setText("Registration successful!");
            AlertHelper.showRegistrationSuccess();

            // Go back to login
            handleBack(event);

        } catch (SQLException e) {
            statusLabel.setText("Database error occurred");
            AlertHelper.showDatabaseError("Registration failed: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("An error occurred");
            AlertHelper.showError("Error", "Registration Error", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle back button click - return to login screen
     * @param event Action event
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Load Login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/LoginView.fxml"));
            Parent root = loader.load();

            // Get current stage and set new scene
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Hospital Appointment System - Login");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load login screen");
            e.printStackTrace();
        }
    }

    /**
     * Check if username already exists in database
     * Direct SQL query without DAO
     *
     * @param username Username to check
     * @return true if username exists, false otherwise
     * @throws SQLException if database error occurs
     */
    private boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM USERS WHERE USERNAME = ?";

        Connection connection = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.setString(1, username);

        ResultSet rs = stmt.executeQuery();
        boolean exists = false;

        if (rs.next()) {
            exists = rs.getInt(1) > 0;
        }

        rs.close();
        stmt.close();

        return exists;
    }

    /**
     * Register a new patient in the database
     * Direct SQL queries without DAO
     * Demonstrates: Exception Handling, PreparedStatement
     *
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email address
     * @param phone Phone number
     * @param dob Date of birth
     * @param bloodGroup Blood group
     * @throws SQLException if database error occurs
     */
    private void registerPatient(String username, String password, String name,
                                 String email, String phone, LocalDate dob,
                                 String bloodGroup) throws SQLException {

        String userSql = "INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, ROLE, NAME, EMAIL, PHONE) " +
                        "VALUES (USER_SEQ.NEXTVAL, ?, ?, 'PATIENT', ?, ?, ?)";

        String patientSql = "INSERT INTO PATIENTS (PATIENT_ID, USER_ID, DATE_OF_BIRTH, BLOOD_GROUP) " +
                           "VALUES (PATIENT_SEQ.NEXTVAL, USER_SEQ.CURRVAL, ?, ?)";

        Connection connection = DatabaseConnection.getInstance().getConnection();

        try {
            // Insert into USERS table
            PreparedStatement userStmt = connection.prepareStatement(userSql);
            userStmt.setString(1, username);
            userStmt.setString(2, password);
            userStmt.setString(3, name);
            userStmt.setString(4, email);
            userStmt.setString(5, phone);
            userStmt.executeUpdate();
            userStmt.close();

            // Insert into PATIENTS table
            PreparedStatement patientStmt = connection.prepareStatement(patientSql);
            patientStmt.setDate(1, Date.valueOf(dob));
            patientStmt.setString(2, bloodGroup);
            patientStmt.executeUpdate();
            patientStmt.close();

            System.out.println("Patient registered successfully: " + username);

        } catch (SQLException e) {
            System.err.println("Error registering patient: " + e.getMessage());
            throw e;
        }
    }
}
