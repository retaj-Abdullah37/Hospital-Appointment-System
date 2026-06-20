package projectcode1.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import projectcode1.dao.DatabaseConnection;
import projectcode1.models.Doctor;
import projectcode1.utils.AlertHelper;

import java.sql.*;
import java.util.ArrayList;

/**
 * BrowseDoctorsController - Controller for Browse Doctors screen
 * Demonstrates: Collections, TableView, Direct SQL
 *
 * @author CS313 Term Project
 */
public class BrowseDoctorsController {

    @FXML
    private ComboBox<String> specialtyCombo;

    @FXML
    private TableView<Doctor> doctorsTable;

    @FXML
    private TableColumn<Doctor, String> nameColumn;

    @FXML
    private TableColumn<Doctor, String> specialtyColumn;

    @FXML
    private TableColumn<Doctor, Integer> experienceColumn;

    @FXML
    private TableColumn<Doctor, String> emailColumn;

    private ObservableList<Doctor> doctorsList;

    /**
     * Initialize method
     */
    @FXML
    public void initialize() {
        // Setup table columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        specialtyColumn.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        experienceColumn.setCellValueFactory(new PropertyValueFactory<>("experienceYears"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load specialties and doctors
        loadSpecialties();
        loadAllDoctors();

        // Add listener to specialty combo box
        specialtyCombo.setOnAction(e -> filterBySpecialty());
    }

    /**
     * Load all specialties for combo box
     */
    private void loadSpecialties() {
        try {
            String sql = "SELECT DISTINCT SPECIALTY FROM DOCTORS ORDER BY SPECIALTY";
            Connection conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ArrayList<String> specialties = new ArrayList<>();
            specialties.add("All Specialties");

            while (rs.next()) {
                specialties.add(rs.getString("SPECIALTY"));
            }

            specialtyCombo.setItems(FXCollections.observableArrayList(specialties));
            specialtyCombo.setValue("All Specialties");

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            AlertHelper.showDatabaseError("Could not load specialties: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all doctors from database
     * Demonstrates: Direct SQL, Collections
     */
    private void loadAllDoctors() {
        try {
            String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                        "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                        "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                        "ORDER BY U.NAME";

            Connection conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            ArrayList<Doctor> doctors = new ArrayList<>();

            while (rs.next()) {
                Doctor doctor = new Doctor(
                    rs.getInt("USER_ID"),
                    rs.getString("USERNAME"),
                    rs.getString("PASSWORD"),
                    rs.getString("NAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PHONE"),
                    rs.getInt("DOCTOR_ID"),
                    rs.getString("SPECIALTY"),
                    rs.getInt("EXPERIENCE_YEARS")
                );
                doctors.add(doctor);
            }

            doctorsList = FXCollections.observableArrayList(doctors);
            doctorsTable.setItems(doctorsList);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            AlertHelper.showDatabaseError("Could not load doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Filter doctors by specialty
     */
    private void filterBySpecialty() {
        String selectedSpecialty = specialtyCombo.getValue();

        if (selectedSpecialty == null || "All Specialties".equals(selectedSpecialty)) {
            loadAllDoctors();
            return;
        }

        try {
            String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                        "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                        "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                        "WHERE D.SPECIALTY = ? " +
                        "ORDER BY U.NAME";

            Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, selectedSpecialty);
            ResultSet rs = stmt.executeQuery();

            ArrayList<Doctor> doctors = new ArrayList<>();

            while (rs.next()) {
                Doctor doctor = new Doctor(
                    rs.getInt("USER_ID"),
                    rs.getString("USERNAME"),
                    rs.getString("PASSWORD"),
                    rs.getString("NAME"),
                    rs.getString("EMAIL"),
                    rs.getString("PHONE"),
                    rs.getInt("DOCTOR_ID"),
                    rs.getString("SPECIALTY"),
                    rs.getInt("EXPERIENCE_YEARS")
                );
                doctors.add(doctor);
            }

            doctorsList = FXCollections.observableArrayList(doctors);
            doctorsTable.setItems(doctorsList);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            AlertHelper.showDatabaseError("Could not filter doctors: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Show All button
     */
    @FXML
    private void handleShowAll() {
        specialtyCombo.setValue("All Specialties");
        loadAllDoctors();
    }

    /**
     * Handle Book Appointment button
     */
    @FXML
    private void handleBookAppointment(ActionEvent event) {
        Doctor selectedDoctor = doctorsTable.getSelectionModel().getSelectedItem();

        if (selectedDoctor == null) {
            AlertHelper.showWarning("No Selection", "No Doctor Selected",
                "Please select a doctor to book an appointment.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/BookAppointment.fxml"));
            Parent root = loader.load();

            // Pass selected doctor to BookAppointment controller
            BookAppointmentController controller = loader.getController();
            controller.setDoctor(selectedDoctor);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Book Appointment");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not load booking screen");
            e.printStackTrace();
        }
    }

    /**
     * Handle Back button
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/PatientDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Patient Dashboard");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error", "Could not return to dashboard");
            e.printStackTrace();
        }
    }
}
