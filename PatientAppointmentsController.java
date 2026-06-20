package projectcode1.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import projectcode1.dao.DatabaseConnection;
import projectcode1.models.Appointment;
import projectcode1.models.Doctor;
import projectcode1.utils.AlertHelper;
import projectcode1.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DoctorScheduleController - Controller for Doctor Schedule View
 * Demonstrates: Collections, Exception Handling, Direct SQL
 *
 * Displays doctor's appointments for selected date
 * Allows adding visit notes and marking appointments as completed
 *
 * @author CS313 Term Project
 */
public class DoctorScheduleController {

    @FXML
    private Label doctorNameLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private TableView<Appointment> appointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdCol;

    @FXML
    private TableColumn<Appointment, String> timeCol;

    @FXML
    private TableColumn<Appointment, String> patientNameCol;

    @FXML
    private TableColumn<Appointment, String> patientPhoneCol;

    @FXML
    private TableColumn<Appointment, String> statusCol;

    @FXML
    private TableColumn<Appointment, String> visitNotesCol;

    @FXML
    private Label statusLabel;

    private ObservableList<Appointment> appointmentsList;
    private Doctor currentDoctor;

    /**
     * Initialize method - called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Get current doctor from session
        currentDoctor = SessionManager.getInstance().getCurrentDoctor();

        if (currentDoctor == null) {
            statusLabel.setText("Error: No doctor logged in");
            return;
        }

        doctorNameLabel.setText("Dr. " + currentDoctor.getName() + " - " + currentDoctor.getSpecialty());

        // Setup table columns
        appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        patientNameCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        patientPhoneCol.setCellValueFactory(new PropertyValueFactory<>("patientPhone"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        visitNotesCol.setCellValueFactory(new PropertyValueFactory<>("visitNotes"));

        // Set default date to today
        datePicker.setValue(LocalDate.now());

        // Load today's appointments
        loadAppointmentsForDate(LocalDate.now());
    }

    /**
     * Handle date picker change
     */
    @FXML
    private void handleDateChanged(ActionEvent event) {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            loadAppointmentsForDate(selectedDate);
        }
    }

    /**
     * Handle Today button
     */
    @FXML
    private void handleToday(ActionEvent event) {
        datePicker.setValue(LocalDate.now());
        loadAppointmentsForDate(LocalDate.now());
    }

    /**
     * Load appointments for the selected date
     * Demonstrates: Direct SQL queries, Collections, Exception Handling
     */
    private void loadAppointmentsForDate(LocalDate date) {
        try {
            // SQL query to get appointments with patient details
            String sql = "SELECT A.APPOINTMENT_ID, A.PATIENT_ID, A.APPOINTMENT_DATE, " +
                        "A.APPOINTMENT_TIME, A.STATUS, A.VISIT_NOTES, " +
                        "U.NAME AS PATIENT_NAME, U.PHONE AS PATIENT_PHONE " +
                        "FROM APPOINTMENTS A " +
                        "JOIN PATIENTS P ON A.PATIENT_ID = P.PATIENT_ID " +
                        "JOIN USERS U ON P.USER_ID = U.USER_ID " +
                        "WHERE A.DOCTOR_ID = ? AND A.APPOINTMENT_DATE = ? " +
                        "ORDER BY A.APPOINTMENT_TIME";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, currentDoctor.getDoctorId());
            stmt.setDate(2, java.sql.Date.valueOf(date));

            ResultSet rs = stmt.executeQuery();

            // Use ArrayList to collect appointments
            ArrayList<Appointment> appointments = new ArrayList<>();

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                appointment.setPatientId(rs.getInt("PATIENT_ID"));
                appointment.setDoctorId(currentDoctor.getDoctorId());
                appointment.setAppointmentDate(rs.getDate("APPOINTMENT_DATE").toLocalDate());
                appointment.setAppointmentTime(rs.getString("APPOINTMENT_TIME"));
                appointment.setStatus(rs.getString("STATUS"));
                appointment.setVisitNotes(rs.getString("VISIT_NOTES"));

                // Set additional properties for display
                appointment.setPatientName(rs.getString("PATIENT_NAME"));
                appointment.setPatientPhone(rs.getString("PATIENT_PHONE"));

                appointments.add(appointment);
            }

            rs.close();
            stmt.close();

            // Convert to ObservableList for TableView
            appointmentsList = FXCollections.observableArrayList(appointments);
            appointmentsTable.setItems(appointmentsList);

            statusLabel.setText("Showing " + appointments.size() + " appointment(s) for " + date);

        } catch (SQLException e) {
            statusLabel.setText("Error loading appointments");
            AlertHelper.showDatabaseError("Could not load appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Add Visit Notes button
     * Opens the Add Visit Notes screen for selected appointment
     */
    @FXML
    private void handleAddVisitNotes(ActionEvent event) {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("Please select an appointment first");
            AlertHelper.showInfo("No Selection", "No Appointment Selected",
                                "Please select an appointment from the table");
            return;
        }

        // Check if appointment is scheduled (can only add notes to scheduled appointments)
        if (!"SCHEDULED".equals(selected.getStatus())) {
            statusLabel.setText("Can only add notes to scheduled appointments");
            AlertHelper.showInfo("Invalid Status", "Cannot Add Notes",
                                "You can only add visit notes to appointments with SCHEDULED status.\n" +
                                "Current status: " + selected.getStatus());
            return;
        }

        try {
            // Load Add Visit Notes view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/AddVisitNotes.fxml"));
            Parent root = loader.load();

            // Pass the selected appointment to the AddVisitNotesController
            AddVisitNotesController controller = loader.getController();
            controller.setAppointment(selected);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Add Visit Notes");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error",
                                 "Could not load Add Visit Notes screen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Mark as Completed button
     * Quick way to mark appointment as completed without notes
     */
    @FXML
    private void handleMarkCompleted(ActionEvent event) {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("Please select an appointment first");
            AlertHelper.showInfo("No Selection", "No Appointment Selected",
                                "Please select an appointment from the table");
            return;
        }

        // Check if already completed
        if ("COMPLETED".equals(selected.getStatus())) {
            statusLabel.setText("This appointment is already completed");
            AlertHelper.showInfo("Already Completed", "Appointment Already Completed",
                                "This appointment has already been marked as completed.");
            return;
        }

        // Confirm completion
        boolean confirmed = AlertHelper.showConfirmation(
            "Confirm Completion",
            "Mark Appointment as Completed?",
            "Patient: " + selected.getPatientName() + "\n" +
            "Time: " + selected.getAppointmentTime() + "\n\n" +
            "Mark this appointment as completed?"
        );

        if (confirmed) {
            markAppointmentCompleted(selected.getAppointmentId(), null);
        }
    }

    /**
     * Mark appointment as completed in database
     * Demonstrates: Direct SQL UPDATE, Exception Handling
     */
    private void markAppointmentCompleted(int appointmentId, String notes) {
        try {
            String sql = "UPDATE APPOINTMENTS SET STATUS = 'COMPLETED'";

            if (notes != null && !notes.trim().isEmpty()) {
                sql += ", VISIT_NOTES = ?";
            }

            sql += " WHERE APPOINTMENT_ID = ?";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            int paramIndex = 1;
            if (notes != null && !notes.trim().isEmpty()) {
                stmt.setString(paramIndex++, notes);
            }
            stmt.setInt(paramIndex, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                statusLabel.setText("Appointment marked as completed");
                AlertHelper.showAppointmentCompleted();

                // Reload appointments
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    loadAppointmentsForDate(selectedDate);
                }

            } else {
                statusLabel.setText("Failed to update appointment");
                AlertHelper.showError("Error", "Update Failed",
                                     "Could not mark appointment as completed. Please try again.");
            }

        } catch (SQLException e) {
            statusLabel.setText("Error updating appointment");
            AlertHelper.showDatabaseError("Could not update appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Back to Dashboard button
     */
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/DoctorDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Doctor Dashboard");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error",
                                 "Could not return to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
