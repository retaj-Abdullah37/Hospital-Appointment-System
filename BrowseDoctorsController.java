package projectcode1.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import projectcode1.dao.DatabaseConnection;
import projectcode1.models.Appointment;
import projectcode1.utils.AlertHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * AddVisitNotesController - Controller for Add Visit Notes View
 * Demonstrates: Exception Handling, Direct SQL UPDATE
 *
 * Allows doctor to add visit notes and mark appointment as completed
 *
 * @author CS313 Term Project
 */
public class AddVisitNotesController {

    @FXML
    private Label appointmentIdLabel;

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private TextArea visitNotesArea;

    @FXML
    private Label statusLabel;

    private Appointment appointment;

    /**
     * Set the appointment to add notes for
     * This method is called from DoctorScheduleController
     * @param appointment The appointment object
     */
    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;

        // Display appointment details
        appointmentIdLabel.setText(String.valueOf(appointment.getAppointmentId()));
        patientNameLabel.setText(appointment.getPatientName());
        dateLabel.setText(appointment.getAppointmentDate().toString());
        timeLabel.setText(appointment.getAppointmentTime());

        // If there are existing notes, display them
        if (appointment.getVisitNotes() != null && !appointment.getVisitNotes().isEmpty()) {
            visitNotesArea.setText(appointment.getVisitNotes());
        }

        statusLabel.setText("");
    }

    /**
     * Handle Save and Mark Completed button
     * Demonstrates: Direct SQL UPDATE, Exception Handling
     */
    @FXML
    private void handleSaveAndComplete(ActionEvent event) {
        String notes = visitNotesArea.getText().trim();

        // Validation
        if (notes.isEmpty()) {
            statusLabel.setText("Please enter visit notes");
            AlertHelper.showValidationError("Please enter visit notes before saving.");
            return;
        }

        if (appointment == null) {
            statusLabel.setText("Error: No appointment selected");
            AlertHelper.showError("Error", "No Appointment", "No appointment is selected.");
            return;
        }

        // Confirm action
        boolean confirmed = AlertHelper.showConfirmation(
            "Confirm Completion",
            "Save Notes and Mark as Completed?",
            "This will mark the appointment as COMPLETED.\n\n" +
            "Patient: " + appointment.getPatientName() + "\n" +
            "Date: " + appointment.getAppointmentDate() + "\n" +
            "Time: " + appointment.getAppointmentTime() + "\n\n" +
            "Continue?"
        );

        if (!confirmed) {
            return;
        }

        // Save notes and mark as completed
        try {
            String sql = "UPDATE APPOINTMENTS SET STATUS = 'COMPLETED', VISIT_NOTES = ? " +
                        "WHERE APPOINTMENT_ID = ?";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, notes);
            stmt.setInt(2, appointment.getAppointmentId());

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                statusLabel.setText("Visit notes saved successfully");
                AlertHelper.showAppointmentCompleted();

                // Return to doctor schedule
                returnToSchedule(event);

            } else {
                statusLabel.setText("Failed to save visit notes");
                AlertHelper.showError("Error", "Save Failed",
                                     "Could not save visit notes. Please try again.");
            }

        } catch (SQLException e) {
            statusLabel.setText("Database error occurred");
            AlertHelper.showDatabaseError("Could not save visit notes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle Cancel button
     * Returns to doctor schedule without saving
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Confirm cancellation if notes have been entered
        String notes = visitNotesArea.getText().trim();
        if (!notes.isEmpty()) {
            boolean confirmed = AlertHelper.showConfirmation(
                "Confirm Cancel",
                "Discard Changes?",
                "You have unsaved visit notes. Are you sure you want to cancel?"
            );

            if (!confirmed) {
                return;
            }
        }

        returnToSchedule(event);
    }

    /**
     * Return to doctor schedule screen
     */
    private void returnToSchedule(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/DoctorSchedule.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("My Schedule");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error",
                                 "Could not return to schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
