package projectcode1.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import projectcode1.dao.DatabaseConnection;
import projectcode1.models.User;
import projectcode1.utils.AlertHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * ManageUsersController - Controller for Admin Manage Users View
 * Demonstrates: Collections, Exception Handling, Direct SQL with filtering
 *
 * Allows admin to view all users with filtering by role
 *
 * @author CS313 Term Project
 */
public class ManageUsersController {

    @FXML
    private ComboBox<String> roleFilterCombo;

    @FXML
    private TableView<UserDisplay> usersTable;

    @FXML
    private TableColumn<UserDisplay, Integer> userIdCol;

    @FXML
    private TableColumn<UserDisplay, String> usernameCol;

    @FXML
    private TableColumn<UserDisplay, String> nameCol;

    @FXML
    private TableColumn<UserDisplay, String> emailCol;

    @FXML
    private TableColumn<UserDisplay, String> phoneCol;

    @FXML
    private TableColumn<UserDisplay, String> roleCol;

    @FXML
    private Label statusLabel;

    private ObservableList<UserDisplay> usersList;

    /**
     * Inner class for displaying user data in TableView
     * Demonstrates: Inner classes, Encapsulation
     */
    public static class UserDisplay {
        private int userId;
        private String username;
        private String name;
        private String email;
        private String phone;
        private String role;

        public UserDisplay(int userId, String username, String name, String email, String phone, String role) {
            this.userId = userId;
            this.username = username;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
        }

        // Getters for PropertyValueFactory
        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
    }

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Setup table columns
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        // Setup role filter combo box
        roleFilterCombo.setItems(FXCollections.observableArrayList(
            "PATIENT", "DOCTOR", "ADMIN"
        ));

        // Load all users
        loadAllUsers();
    }

    /**
     * Load all users from database
     * Demonstrates: Direct SQL, Collections, Exception Handling
     */
    private void loadAllUsers() {
        try {
            String sql = "SELECT USER_ID, USERNAME, NAME, EMAIL, PHONE, ROLE " +
                        "FROM USERS " +
                        "ORDER BY ROLE, NAME";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            // Use ArrayList to collect users
            ArrayList<UserDisplay> users = new ArrayList<>();

            while (rs.next()) {
                UserDisplay user = new UserDisplay(
                    rs.getInt("USER_ID"),
                    rs.getString("USERNAME"),
                    rs.getString("NAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PHONE"),
                    rs.getString("ROLE")
                );
                users.add(user);
            }

            rs.close();
            stmt.close();

            // Convert to ObservableList for TableView
            usersList = FXCollections.observableArrayList(users);
            usersTable.setItems(usersList);

            statusLabel.setText("Total users: " + users.size());

        } catch (SQLException e) {
            statusLabel.setText("Error loading users");
            AlertHelper.showDatabaseError("Could not load users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load users filtered by role
     * Demonstrates: Direct SQL with WHERE clause, Exception Handling
     */
    private void loadUsersByRole(String role) {
        try {
            String sql = "SELECT USER_ID, USERNAME, NAME, EMAIL, PHONE, ROLE " +
                        "FROM USERS " +
                        "WHERE ROLE = ? " +
                        "ORDER BY NAME";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, role);

            ResultSet rs = stmt.executeQuery();

            ArrayList<UserDisplay> users = new ArrayList<>();

            while (rs.next()) {
                UserDisplay user = new UserDisplay(
                    rs.getInt("USER_ID"),
                    rs.getString("USERNAME"),
                    rs.getString("NAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PHONE"),
                    rs.getString("ROLE")
                );
                users.add(user);
            }

            rs.close();
            stmt.close();

            usersList = FXCollections.observableArrayList(users);
            usersTable.setItems(usersList);

            statusLabel.setText("Showing " + users.size() + " " + role + " user(s)");

        } catch (SQLException e) {
            statusLabel.setText("Error loading users");
            AlertHelper.showDatabaseError("Could not load users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle filter change
     */
    @FXML
    private void handleFilterChanged(ActionEvent event) {
        String selectedRole = roleFilterCombo.getValue();
        if (selectedRole != null && !selectedRole.isEmpty()) {
            loadUsersByRole(selectedRole);
        }
    }

    /**
     * Handle Show All button
     */
    @FXML
    private void handleShowAll(ActionEvent event) {
        roleFilterCombo.setValue(null);
        loadAllUsers();
    }

    /**
     * Handle View Details button
     */
    @FXML
    private void handleViewDetails(ActionEvent event) {
        UserDisplay selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("Please select a user first");
            AlertHelper.showInfo("No Selection", "No User Selected",
                                "Please select a user from the table");
            return;
        }

        // Show user details
        StringBuilder details = new StringBuilder();
        details.append("User ID: ").append(selected.getUserId()).append("\n");
        details.append("Username: ").append(selected.getUsername()).append("\n");
        details.append("Name: ").append(selected.getName()).append("\n");
        details.append("Email: ").append(selected.getEmail()).append("\n");
        details.append("Phone: ").append(selected.getPhone()).append("\n");
        details.append("Role: ").append(selected.getRole()).append("\n");

        AlertHelper.showInfo("User Details",
                            "Details for " + selected.getName(),
                            details.toString());
    }

    /**
     * Handle Back to Dashboard button
     */
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/AdminDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error",
                                 "Could not return to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
