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
import projectcode1.models.Appointment;
import projectcode1.utils.AlertHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * ViewAllAppointmentsController - Controller for Admin View All Appointments
 * Demonstrates: Collections, Exception Handling, Direct SQL with filtering
 *
 * Allows admin to view all appointments with filtering options
 *
 * @author CS313 Term Project
 */
public class ViewAllAppointmentsController {

    @FXML
    private ComboBox<String> statusFilterCombo;

    @FXML
    private TableView<Appointment> appointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdCol;

    @FXML
    private TableColumn<Appointment, String> patientNameCol;

    @FXML
    private TableColumn<Appointment, String> doctorNameCol;

    @FXML
    private TableColumn<Appointment, String> specialtyCol;

    @FXML
    private TableColumn<Appointment, LocalDate> dateCol;

    @FXML
    private TableColumn<Appointment, String> timeCol;

    @FXML
    private TableColumn<Appointment, String> statusCol;

    @FXML
    private Label statusLabel;

    private ObservableList<Appointment> appointmentsList;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Setup table columns
        appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        patientNameCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorNameCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        specialtyCol.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup status filter combo box
        statusFilterCombo.setItems(FXCollections.observableArrayList(
            "SCHEDULED", "COMPLETED", "CANCELLED"
        ));

        // Load all appointments
        loadAllAppointments();
    }

    /**
     * Load all appointments from database
     * Demonstrates: Direct SQL with JOINs, Collections, Exception Handling
     */
    private void loadAllAppointments() {
        try {
            // Complex SQL query with multiple JOINs
            String sql = "SELECT A.APPOINTMENT_ID, A.APPOINTMENT_DATE, A.APPOINTMENT_TIME, A.STATUS, " +
                        "UP.NAME AS PATIENT_NAME, UD.NAME AS DOCTOR_NAME, D.SPECIALTY " +
                        "FROM APPOINTMENTS A " +
                        "JOIN PATIENTS P ON A.PATIENT_ID = P.PATIENT_ID " +
                        "JOIN USERS UP ON P.USER_ID = UP.USER_ID " +
                        "JOIN DOCTORS D ON A.DOCTOR_ID = D.DOCTOR_ID " +
                        "JOIN USERS UD ON D.USER_ID = UD.USER_ID " +
                        "ORDER BY A.APPOINTMENT_DATE DESC, A.APPOINTMENT_TIME DESC";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            // Use ArrayList to collect appointments
            ArrayList<Appointment> appointments = new ArrayList<>();

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                appointment.setAppointmentDate(rs.getDate("APPOINTMENT_DATE").toLocalDate());
                appointment.setAppointmentTime(rs.getString("APPOINTMENT_TIME"));
                appointment.setStatus(rs.getString("STATUS"));

                // Set display fields
                appointment.setPatientName(rs.getString("PATIENT_NAME"));
                appointment.setDoctorName(rs.getString("DOCTOR_NAME"));
                appointment.setSpecialty(rs.getString("SPECIALTY"));

                appointments.add(appointment);
            }

            rs.close();
            stmt.close();

            // Convert to ObservableList for TableView
            appointmentsList = FXCollections.observableArrayList(appointments);
            appointmentsTable.setItems(appointmentsList);

            statusLabel.setText("Total appointments: " + appointments.size());

        } catch (SQLException e) {
            statusLabel.setText("Error loading appointments");
            AlertHelper.showDatabaseError("Could not load appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load appointments filtered by status
     * Demonstrates: Direct SQL with WHERE clause
     */
    private void loadAppointmentsByStatus(String status) {
        try {
            String sql = "SELECT A.APPOINTMENT_ID, A.APPOINTMENT_DATE, A.APPOINTMENT_TIME, A.STATUS, " +
                        "UP.NAME AS PATIENT_NAME, UD.NAME AS DOCTOR_NAME, D.SPECIALTY " +
                        "FROM APPOINTMENTS A " +
                        "JOIN PATIENTS P ON A.PATIENT_ID = P.PATIENT_ID " +
                        "JOIN USERS UP ON P.USER_ID = UP.USER_ID " +
                        "JOIN DOCTORS D ON A.DOCTOR_ID = D.DOCTOR_ID " +
                        "JOIN USERS UD ON D.USER_ID = UD.USER_ID " +
                        "WHERE A.STATUS = ? " +
                        "ORDER BY A.APPOINTMENT_DATE DESC, A.APPOINTMENT_TIME DESC";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, status);

            ResultSet rs = stmt.executeQuery();

            ArrayList<Appointment> appointments = new ArrayList<>();

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                appointment.setAppointmentDate(rs.getDate("APPOINTMENT_DATE").toLocalDate());
                appointment.setAppointmentTime(rs.getString("APPOINTMENT_TIME"));
                appointment.setStatus(rs.getString("STATUS"));

                appointment.setPatientName(rs.getString("PATIENT_NAME"));
                appointment.setDoctorName(rs.getString("DOCTOR_NAME"));
                appointment.setSpecialty(rs.getString("SPECIALTY"));

                appointments.add(appointment);
            }

            rs.close();
            stmt.close();

            appointmentsList = FXCollections.observableArrayList(appointments);
            appointmentsTable.setItems(appointmentsList);

            statusLabel.setText("Showing " + appointments.size() + " " + status + " appointment(s)");

        } catch (SQLException e) {
            statusLabel.setText("Error loading appointments");
            AlertHelper.showDatabaseError("Could not load appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle filter change
     */
    @FXML
    private void handleFilterChanged(ActionEvent event) {
        String selectedStatus = statusFilterCombo.getValue();
        if (selectedStatus != null && !selectedStatus.isEmpty()) {
            loadAppointmentsByStatus(selectedStatus);
        }
    }

    /**
     * Handle Show All button
     */
    @FXML
    private void handleShowAll(ActionEvent event) {
        statusFilterCombo.setValue(null);
        loadAllAppointments();
    }

    /**
     * Handle View Details button
     */
    @FXML
    private void handleViewDetails(ActionEvent event) {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("Please select an appointment first");
            AlertHelper.showInfo("No Selection", "No Appointment Selected",
                                "Please select an appointment from the table");
            return;
        }

        // Show appointment details
        StringBuilder details = new StringBuilder();
        details.append("Appointment ID: ").append(selected.getAppointmentId()).append("\n");
        details.append("Patient: ").append(selected.getPatientName()).append("\n");
        details.append("Doctor: ").append(selected.getDoctorName()).append("\n");
        details.append("Specialty: ").append(selected.getSpecialty()).append("\n");
        details.append("Date: ").append(selected.getAppointmentDate()).append("\n");
        details.append("Time: ").append(selected.getAppointmentTime()).append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n");

        AlertHelper.showInfo("Appointment Details",
                            "Details for Appointment #" + selected.getAppointmentId(),
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
