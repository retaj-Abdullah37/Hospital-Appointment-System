package projectcode1.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import projectcode1.dao.DatabaseConnection;
import projectcode1.models.Appointment;
import projectcode1.models.Patient;
import projectcode1.utils.AlertHelper;
import projectcode1.utils.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * PatientAppointmentsController - Controller for Patient Appointments View
 * Demonstrates: Collections (ArrayList, ObservableList), Exception Handling, Direct SQL
 *
 * Displays all appointments for the logged-in patient
 * Allows canceling upcoming appointments
 * Shows visit notes for completed appointments
 *
 * @author CS313 Term Project
 */
public class PatientAppointmentsController {

    @FXML
    private Label patientNameLabel;

    @FXML
    private TableView<Appointment> appointmentsTable;

    @FXML
    private TableColumn<Appointment, Integer> appointmentIdCol;

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

    @FXML
    private VBox visitNotesBox;

    @FXML
    private Label visitNotesLabel;

    private ObservableList<Appointment> appointmentsList;
    private Patient currentPatient;

    /**
     * Initialize method - called automatically after FXML is loaded
     * Demonstrates: Collections initialization
     */
    @FXML
    public void initialize() {
        // Get current patient from session
        currentPatient = SessionManager.getInstance().getCurrentPatient();

        if (currentPatient == null) {
            statusLabel.setText("Error: No patient logged in");
            return;
        }

        patientNameLabel.setText("Patient: " + currentPatient.getName());

        // Setup table columns with PropertyValueFactory
        appointmentIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        doctorNameCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        specialtyCol.setCellValueFactory(new PropertyValueFactory<>("specialty"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDate"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Load patient's appointments
        loadPatientAppointments();
    }

    /**
     * Load all appointments for the current patient from database
     * Demonstrates: Direct SQL queries, Collections (ArrayList), Exception Handling
     */
    private void loadPatientAppointments() {
        try {
            // SQL query to get all appointments with doctor details
            String sql = "SELECT A.APPOINTMENT_ID, A.PATIENT_ID, A.DOCTOR_ID, " +
                        "A.APPOINTMENT_DATE, A.APPOINTMENT_TIME, A.STATUS, A.VISIT_NOTES, " +
                        "U.NAME AS DOCTOR_NAME, D.SPECIALTY " +
                        "FROM APPOINTMENTS A " +
                        "JOIN DOCTORS D ON A.DOCTOR_ID = D.DOCTOR_ID " +
                        "JOIN USERS U ON D.USER_ID = U.USER_ID " +
                        "WHERE A.PATIENT_ID = ? " +
                        "ORDER BY A.APPOINTMENT_DATE DESC, A.APPOINTMENT_TIME DESC";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, currentPatient.getPatientId());

            ResultSet rs = stmt.executeQuery();

            // Use ArrayList to collect appointments
            ArrayList<Appointment> appointments = new ArrayList<>();

            while (rs.next()) {
                Appointment appointment = new Appointment();
                appointment.setAppointmentId(rs.getInt("APPOINTMENT_ID"));
                appointment.setPatientId(rs.getInt("PATIENT_ID"));
                appointment.setDoctorId(rs.getInt("DOCTOR_ID"));
                appointment.setAppointmentDate(rs.getDate("APPOINTMENT_DATE").toLocalDate());
                appointment.setAppointmentTime(rs.getString("APPOINTMENT_TIME"));
                appointment.setStatus(rs.getString("STATUS"));
                appointment.setVisitNotes(rs.getString("VISIT_NOTES"));

                // Set additional properties for display
                appointment.setDoctorName(rs.getString("DOCTOR_NAME"));
                appointment.setSpecialty(rs.getString("SPECIALTY"));

                appointments.add(appointment);
            }

            rs.close();
            stmt.close();

            // Convert ArrayList to ObservableList for TableView
            appointmentsList = FXCollections.observableArrayList(appointments);
            appointmentsTable.setItems(appointmentsList);

            statusLabel.setText("Loaded " + appointments.size() + " appointment(s)");

        } catch (SQLException e) {
            statusLabel.setText("Error loading appointments");
            AlertHelper.showDatabaseError("Could not load appointments: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle view details button click
     * Shows visit notes if appointment is completed
     * @param event Action event
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
        details.append("Doctor: ").append(selected.getDoctorName()).append("\n");
        details.append("Specialty: ").append(selected.getSpecialty()).append("\n");
        details.append("Date: ").append(selected.getAppointmentDate()).append("\n");
        details.append("Time: ").append(selected.getAppointmentTime()).append("\n");
        details.append("Status: ").append(selected.getStatus()).append("\n");

        // Show visit notes if completed
        if ("COMPLETED".equals(selected.getStatus()) && selected.getVisitNotes() != null) {
            details.append("\nVisit Notes:\n").append(selected.getVisitNotes());

            // Also display in the notes section
            visitNotesLabel.setText(selected.getVisitNotes());
            visitNotesBox.setVisible(true);
            visitNotesBox.setManaged(true);
        } else {
            visitNotesBox.setVisible(false);
            visitNotesBox.setManaged(false);

            if ("SCHEDULED".equals(selected.getStatus())) {
                details.append("\nThis appointment has not been completed yet.");
            } else if ("CANCELLED".equals(selected.getStatus())) {
                details.append("\nThis appointment was cancelled.");
            }
        }

        AlertHelper.showInfo("Appointment Details", "Details for Appointment #" + selected.getAppointmentId(),
                            details.toString());
    }

    /**
     * Handle cancel appointment button click
     * Only allows canceling scheduled appointments
     * Demonstrates: Exception Handling, Direct SQL UPDATE
     * @param event Action event
     */
    @FXML
    private void handleCancelAppointment(ActionEvent event) {
        Appointment selected = appointmentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            statusLabel.setText("Please select an appointment to cancel");
            AlertHelper.showInfo("No Selection", "No Appointment Selected",
                                "Please select an appointment from the table");
            return;
        }

        // Check if appointment can be cancelled
        if (!"SCHEDULED".equals(selected.getStatus())) {
            statusLabel.setText("Can only cancel scheduled appointments");
            AlertHelper.showInfo("Cannot Cancel", "Appointment Cannot Be Cancelled",
                                "You can only cancel appointments with SCHEDULED status.\n" +
                                "Current status: " + selected.getStatus());
            return;
        }

        // Confirm cancellation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Cancellation");
        confirmAlert.setHeaderText("Cancel Appointment?");
        confirmAlert.setContentText("Are you sure you want to cancel this appointment?\n\n" +
                                   "Doctor: " + selected.getDoctorName() + "\n" +
                                   "Date: " + selected.getAppointmentDate() + "\n" +
                                   "Time: " + selected.getAppointmentTime());

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Cancel the appointment
            cancelAppointment(selected.getAppointmentId());
        }
    }

    /**
     * Cancel appointment by updating status to CANCELLED
     * Demonstrates: Direct SQL UPDATE, Exception Handling
     * @param appointmentId Appointment ID to cancel
     */
    private void cancelAppointment(int appointmentId) {
        try {
            String sql = "UPDATE APPOINTMENTS SET STATUS = 'CANCELLED' WHERE APPOINTMENT_ID = ?";

            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, appointmentId);

            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                statusLabel.setText("Appointment cancelled successfully");
                AlertHelper.showSuccess("Success", "Appointment Cancelled",
                                       "Your appointment has been cancelled successfully.");

                // Reload appointments to reflect changes
                loadPatientAppointments();

            } else {
                statusLabel.setText("Failed to cancel appointment");
                AlertHelper.showError("Error", "Cancellation Failed",
                                     "Could not cancel the appointment. Please try again.");
            }

        } catch (SQLException e) {
            statusLabel.setText("Error cancelling appointment");
            AlertHelper.showDatabaseError("Could not cancel appointment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle back to dashboard button click
     * Returns to patient dashboard
     * @param event Action event
     */
    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/projectcode1/views/PatientDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Patient Dashboard");
            stage.show();

        } catch (Exception e) {
            AlertHelper.showError("Error", "Navigation Error",
                                 "Could not return to dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
