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
import projectcode1.models.Doctor;
import projectcode1.models.Patient;
import projectcode1.utils.SessionManager;
import projectcode1.utils.AlertHelper;

import java.sql.*;
import java.time.LocalDate;

/**
 * BookAppointmentController - Controller for booking appointments
 * Demonstrates: Exception Handling, Direct SQL
 *
 * @author CS313 Term Project
 */
public class BookAppointmentController {

    @FXML
    private Label doctorNameLabel;

    @FXML
    private Label doctorSpecialtyLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private ComboBox<String> timeCombo;

    @FXML
    private Label statusLabel;

    private Doctor selectedDoctor;

    /**
     * Initialize method
     */
    @FXML
    public void initialize() {
        statusLabel.setText("");

        // Populate time slots
        timeCombo.getItems().addAll(
            "09:00", "10:00", "11:00", "12:00",
            "14:00", "15:00", "16:00", "17:00"
        );

        // Set minimum date to today
        datePicker.setValue(LocalDate.now());
    }

    /**
     * Set the selected doctor (called from BrowseDoctors screen)
     */
    public void setDoctor(Doctor doctor) {
        this.selectedDoctor = doctor;
        doctorNameLabel.setText("Doctor: " + doctor.getName());
        doctorSpecialtyLabel.setText("Specialty: " + doctor.getSpecialty());
    }

    /**
     * Handle Confirm Booking button
     */
    @FXML
    private void handleConfirmBooking(ActionEvent event) {
        LocalDate selectedDate = datePicker.getValue();
        String selectedTime = timeCombo.getValue();

        // Validation
        if (selectedDate == null) {
            statusLabel.setText("Please select a date");
            AlertHelper.showValidationError("Please select an appointment date");
            return;
        }

        if (selectedTime == null) {
            statusLabel.setText("Please select a time");
            AlertHelper.showValidationError("Please select an appointment time");
            return;
        }

        // Check if date is not in the past
        if (selectedDate.isBefore(LocalDate.now())) {
            statusLabel.setText("Cannot book appointments in the past");
            AlertHelper.showValidationError("Please select a future date");
            return;
        }

        try {
            // Get current patient from session
            Patient patient = SessionManager.getInstance().getCurrentPatient();

            if (patient == null) {
                AlertHelper.showError("Error", "Session Error", "Please login again");
                return;
            }

            // Check if time slot is available
            if (isTimeSlotTaken(selectedDoctor.getDoctorId(), selectedDate, selectedTime)) {
                statusLabel.setText("This time slot is already booked");
                AlertHelper.showWarning("Time Unavailable", "Slot Already Booked",
                    "This time slot is already taken. Please choose another time.");
                return;
            }

            // Book the appointment
            bookAppointment(patient.getPatientId(), selectedDoctor.getDoctorId(),
                          selectedDate, selectedTime);

            statusLabel.setText("Appointment booked successfully!");
            AlertHelper.showAppointmentBooked();

            // Return to patient dashboard
            handleCancel(event);

        } catch (SQLException e) {
            statusLabel.setText("Database error occurred");
            AlertHelper.showDatabaseError("Could not book appointment: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            statusLabel.setText("An error occurred");
            AlertHelper.showError("Error", "Booking Error", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if time slot is already taken
     * Direct SQL query
     */
    private boolean isTimeSlotTaken(int doctorId, LocalDate date, String time) throws SQLException {
        String sql = "SELECT COUNT(*) FROM APPOINTMENTS " +
                    "WHERE DOCTOR_ID = ? AND APPOINTMENT_DATE = ? AND APPOINTMENT_TIME = ? " +
                    "AND STATUS = 'SCHEDULED'";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, doctorId);
        stmt.setDate(2, Date.valueOf(date));
        stmt.setString(3, time);

        ResultSet rs = stmt.executeQuery();
        boolean isTaken = false;

        if (rs.next()) {
            isTaken = rs.getInt(1) > 0;
        }

        rs.close();
        stmt.close();

        return isTaken;
    }

    /**
     * Book appointment in database
     * Direct SQL query
     */
    private void bookAppointment(int patientId, int doctorId, LocalDate date, String time)
            throws SQLException {

        String sql = "INSERT INTO APPOINTMENTS " +
                    "(APPOINTMENT_ID, PATIENT_ID, DOCTOR_ID, APPOINTMENT_DATE, APPOINTMENT_TIME, STATUS) " +
                    "VALUES (APPOINTMENT_SEQ.NEXTVAL, ?, ?, ?, ?, 'SCHEDULED')";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, patientId);
        stmt.setInt(2, doctorId);
        stmt.setDate(3, Date.valueOf(date));
        stmt.setString(4, time);

        stmt.executeUpdate();
        stmt.close();

        System.out.println("Appointment booked: Patient " + patientId +
                          " with Doctor " + doctorId + " on " + date + " at " + time);
    }

    /**
     * Handle Cancel button
     */
    @FXML
    private void handleCancel(ActionEvent event) {
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
