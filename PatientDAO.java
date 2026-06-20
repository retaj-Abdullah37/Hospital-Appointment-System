package projectcode1.dao;

import projectcode1.models.Appointment;
import projectcode1.models.Patient;
import projectcode1.models.Doctor;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * AppointmentDAO class - Data Access Object for Appointment entity
 * Demonstrates: Interface implementation, Exception Handling, Collections
 *
 * Implements Manageable<Appointment> interface for CRUD operations
 *
 * @author CS313 Term Project
 */
public class AppointmentDAO implements Manageable<Appointment> {

    private Connection connection;
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;

    /**
     * Constructor - gets database connection and initializes DAOs
     */
    public AppointmentDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
            this.patientDAO = new PatientDAO();
            this.doctorDAO = new DoctorDAO();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection in AppointmentDAO!");
            e.printStackTrace();
        }
    }

    /**
     * Add a new appointment to the database
     * @param appointment Appointment object to add
     * @throws SQLException if database error occurs
     */
    @Override
    public void add(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO APPOINTMENTS (APPOINTMENT_ID, PATIENT_ID, DOCTOR_ID, " +
                    "APPOINTMENT_DATE, APPOINTMENT_TIME, STATUS) " +
                    "VALUES (APPOINTMENT_SEQ.NEXTVAL, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setString(4, appointment.getAppointmentTime());
            stmt.setString(5, appointment.getStatus());
            stmt.executeUpdate();
            stmt.close();

            System.out.println("Appointment added successfully!");

        } catch (SQLException e) {
            System.err.println("Error adding appointment: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing appointment in the database
     * @param appointment Appointment object with updated information
     * @throws SQLException if database error occurs
     */
    @Override
    public void update(Appointment appointment) throws SQLException {
        String sql = "UPDATE APPOINTMENTS SET PATIENT_ID=?, DOCTOR_ID=?, " +
                    "APPOINTMENT_DATE=?, APPOINTMENT_TIME=?, STATUS=?, VISIT_NOTES=? " +
                    "WHERE APPOINTMENT_ID=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, appointment.getPatientId());
            stmt.setInt(2, appointment.getDoctorId());
            stmt.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
            stmt.setString(4, appointment.getAppointmentTime());
            stmt.setString(5, appointment.getStatus());
            stmt.setString(6, appointment.getVisitNotes());
            stmt.setInt(7, appointment.getAppointmentId());
            stmt.executeUpdate();
            stmt.close();

            System.out.println("Appointment updated successfully!");

        } catch (SQLException e) {
            System.err.println("Error updating appointment: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete an appointment by ID
     * @param appointmentId Appointment ID to delete
     * @throws SQLException if database error occurs
     */
    @Override
    public void delete(int appointmentId) throws SQLException {
        String sql = "DELETE FROM APPOINTMENTS WHERE APPOINTMENT_ID=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                System.out.println("Appointment deleted successfully!");
            } else {
                System.out.println("Appointment not found!");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting appointment: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get an appointment by ID
     * @param appointmentId Appointment ID
     * @return Appointment object, or null if not found
     * @throws SQLException if database error occurs
     */
    @Override
    public Appointment getById(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE APPOINTMENT_ID = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            Appointment appointment = null;
            if (rs.next()) {
                appointment = extractAppointmentFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return appointment;

        } catch (SQLException e) {
            System.err.println("Error getting appointment by ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get all appointments from the database
     * @return ArrayList of Appointment objects
     * @throws SQLException if database error occurs
     */
    @Override
    public ArrayList<Appointment> getAll() throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS ORDER BY APPOINTMENT_DATE, APPOINTMENT_TIME";
        ArrayList<Appointment> appointments = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

            rs.close();
            stmt.close();
            return appointments;

        } catch (SQLException e) {
            System.err.println("Error getting all appointments: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get appointments by patient ID
     * @param patientId Patient ID
     * @return ArrayList of Appointment objects
     * @throws SQLException if database error occurs
     */
    public ArrayList<Appointment> getByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE PATIENT_ID = ? " +
                    "ORDER BY APPOINTMENT_DATE DESC, APPOINTMENT_TIME DESC";
        ArrayList<Appointment> appointments = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

            rs.close();
            stmt.close();
            return appointments;

        } catch (SQLException e) {
            System.err.println("Error getting appointments by patient ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get appointments by doctor ID
     * @param doctorId Doctor ID
     * @return ArrayList of Appointment objects
     * @throws SQLException if database error occurs
     */
    public ArrayList<Appointment> getByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE DOCTOR_ID = ? " +
                    "ORDER BY APPOINTMENT_DATE, APPOINTMENT_TIME";
        ArrayList<Appointment> appointments = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

            rs.close();
            stmt.close();
            return appointments;

        } catch (SQLException e) {
            System.err.println("Error getting appointments by doctor ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get appointments by doctor ID and date
     * @param doctorId Doctor ID
     * @param date Appointment date
     * @return ArrayList of Appointment objects
     * @throws SQLException if database error occurs
     */
    public ArrayList<Appointment> getByDoctorAndDate(int doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE DOCTOR_ID = ? AND APPOINTMENT_DATE = ? " +
                    "ORDER BY APPOINTMENT_TIME";
        ArrayList<Appointment> appointments = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

            rs.close();
            stmt.close();
            return appointments;

        } catch (SQLException e) {
            System.err.println("Error getting appointments by doctor and date: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get appointments by status
     * @param status Appointment status (SCHEDULED, COMPLETED, CANCELLED)
     * @return ArrayList of Appointment objects
     * @throws SQLException if database error occurs
     */
    public ArrayList<Appointment> getByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM APPOINTMENTS WHERE STATUS = ? " +
                    "ORDER BY APPOINTMENT_DATE, APPOINTMENT_TIME";
        ArrayList<Appointment> appointments = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment appointment = extractAppointmentFromResultSet(rs);
                appointments.add(appointment);
            }

            rs.close();
            stmt.close();
            return appointments;

        } catch (SQLException e) {
            System.err.println("Error getting appointments by status: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Cancel an appointment
     * @param appointmentId Appointment ID to cancel
     * @throws SQLException if database error occurs
     */
    public void cancelAppointment(int appointmentId) throws SQLException {
        String sql = "UPDATE APPOINTMENTS SET STATUS='CANCELLED' WHERE APPOINTMENT_ID=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            stmt.executeUpdate();
            stmt.close();

            System.out.println("Appointment cancelled successfully!");

        } catch (SQLException e) {
            System.err.println("Error cancelling appointment: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Complete an appointment and add visit notes
     * @param appointmentId Appointment ID
     * @param visitNotes Visit notes from doctor
     * @throws SQLException if database error occurs
     */
    public void completeAppointment(int appointmentId, String visitNotes) throws SQLException {
        String sql = "UPDATE APPOINTMENTS SET STATUS='COMPLETED', VISIT_NOTES=? " +
                    "WHERE APPOINTMENT_ID=?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, visitNotes);
            stmt.setInt(2, appointmentId);
            stmt.executeUpdate();
            stmt.close();

            System.out.println("Appointment marked as completed!");

        } catch (SQLException e) {
            System.err.println("Error completing appointment: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to extract Appointment object from ResultSet
     * @param rs ResultSet containing appointment data
     * @return Appointment object
     * @throws SQLException if database error occurs
     */
    private Appointment extractAppointmentFromResultSet(ResultSet rs) throws SQLException {
        int appointmentId = rs.getInt("APPOINTMENT_ID");
        int patientId = rs.getInt("PATIENT_ID");
        int doctorId = rs.getInt("DOCTOR_ID");

        Date appointmentDate = rs.getDate("APPOINTMENT_DATE");
        LocalDate date = (appointmentDate != null) ? appointmentDate.toLocalDate() : null;

        String time = rs.getString("APPOINTMENT_TIME");
        String status = rs.getString("STATUS");
        String visitNotes = rs.getString("VISIT_NOTES");

        Appointment appointment = new Appointment(appointmentId, patientId, doctorId,
                                                  date, time, status, visitNotes);

        // Optionally load patient and doctor objects (Composition)
        try {
            Patient patient = patientDAO.getByPatientId(patientId);
            Doctor doctor = doctorDAO.getByDoctorId(doctorId);
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
        } catch (SQLException e) {
            // If loading patient/doctor fails, continue without them
            System.err.println("Warning: Could not load patient/doctor for appointment " + appointmentId);
        }

        return appointment;
    }
}
