package projectcode1.dao;

import projectcode1.models.Patient;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * PatientDAO class - Data Access Object for Patient entity
 * Demonstrates: Interface implementation, Exception Handling, Collections
 *
 * Implements Manageable<Patient> interface for CRUD operations
 *
 * @author CS313 Term Project
 */
public class PatientDAO implements Manageable<Patient> {

    private Connection connection;

    /**
     * Constructor - gets database connection
     */
    public PatientDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection in PatientDAO!");
            e.printStackTrace();
        }
    }

    /**
     * Add a new patient to the database
     * @param patient Patient object to add
     * @throws SQLException if database error occurs
     */
    @Override
    public void add(Patient patient) throws SQLException {
        String userSql = "INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, ROLE, NAME, EMAIL, PHONE) " +
                        "VALUES (USER_SEQ.NEXTVAL, ?, ?, 'PATIENT', ?, ?, ?)";
        String patientSql = "INSERT INTO PATIENTS (PATIENT_ID, USER_ID, DATE_OF_BIRTH, BLOOD_GROUP) " +
                           "VALUES (PATIENT_SEQ.NEXTVAL, USER_SEQ.CURRVAL, ?, ?)";

        try {
            // Insert into USERS table
            PreparedStatement userStmt = connection.prepareStatement(userSql);
            userStmt.setString(1, patient.getUsername());
            userStmt.setString(2, patient.getPassword());
            userStmt.setString(3, patient.getName());
            userStmt.setString(4, patient.getEmail());
            userStmt.setString(5, patient.getPhone());
            userStmt.executeUpdate();
            userStmt.close();

            // Insert into PATIENTS table
            PreparedStatement patientStmt = connection.prepareStatement(patientSql);
            patientStmt.setDate(1, patient.getDateOfBirth() != null ?
                               Date.valueOf(patient.getDateOfBirth()) : null);
            patientStmt.setString(2, patient.getBloodGroup());
            patientStmt.executeUpdate();
            patientStmt.close();

            System.out.println("Patient added successfully!");

        } catch (SQLException e) {
            System.err.println("Error adding patient: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing patient in the database
     * @param patient Patient object with updated information
     * @throws SQLException if database error occurs
     */
    @Override
    public void update(Patient patient) throws SQLException {
        String userSql = "UPDATE USERS SET USERNAME=?, PASSWORD=?, NAME=?, EMAIL=?, PHONE=? " +
                        "WHERE USER_ID=?";
        String patientSql = "UPDATE PATIENTS SET DATE_OF_BIRTH=?, BLOOD_GROUP=? " +
                           "WHERE USER_ID=?";

        try {
            // Update USERS table
            PreparedStatement userStmt = connection.prepareStatement(userSql);
            userStmt.setString(1, patient.getUsername());
            userStmt.setString(2, patient.getPassword());
            userStmt.setString(3, patient.getName());
            userStmt.setString(4, patient.getEmail());
            userStmt.setString(5, patient.getPhone());
            userStmt.setInt(6, patient.getUserId());
            userStmt.executeUpdate();
            userStmt.close();

            // Update PATIENTS table
            PreparedStatement patientStmt = connection.prepareStatement(patientSql);
            patientStmt.setDate(1, patient.getDateOfBirth() != null ?
                               Date.valueOf(patient.getDateOfBirth()) : null);
            patientStmt.setString(2, patient.getBloodGroup());
            patientStmt.setInt(3, patient.getUserId());
            patientStmt.executeUpdate();
            patientStmt.close();

            System.out.println("Patient updated successfully!");

        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a patient by ID
     * @param userId User ID of the patient to delete
     * @throws SQLException if database error occurs
     */
    @Override
    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM USERS WHERE USER_ID=? AND ROLE='PATIENT'";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                System.out.println("Patient deleted successfully!");
            } else {
                System.out.println("Patient not found!");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get a patient by user ID
     * @param userId User ID of the patient
     * @return Patient object, or null if not found
     * @throws SQLException if database error occurs
     */
    @Override
    public Patient getById(int userId) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "P.PATIENT_ID, P.DATE_OF_BIRTH, P.BLOOD_GROUP " +
                    "FROM USERS U JOIN PATIENTS P ON U.USER_ID = P.USER_ID " +
                    "WHERE U.USER_ID = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            Patient patient = null;
            if (rs.next()) {
                patient = extractPatientFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return patient;

        } catch (SQLException e) {
            System.err.println("Error getting patient by ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get all patients from the database
     * @return ArrayList of Patient objects
     * @throws SQLException if database error occurs
     */
    @Override
    public ArrayList<Patient> getAll() throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "P.PATIENT_ID, P.DATE_OF_BIRTH, P.BLOOD_GROUP " +
                    "FROM USERS U JOIN PATIENTS P ON U.USER_ID = P.USER_ID " +
                    "ORDER BY U.NAME";

        ArrayList<Patient> patients = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Patient patient = extractPatientFromResultSet(rs);
                patients.add(patient);
            }

            rs.close();
            stmt.close();
            return patients;

        } catch (SQLException e) {
            System.err.println("Error getting all patients: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get patient by username
     * @param username Username to search for
     * @return Patient object, or null if not found
     * @throws SQLException if database error occurs
     */
    public Patient getByUsername(String username) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "P.PATIENT_ID, P.DATE_OF_BIRTH, P.BLOOD_GROUP " +
                    "FROM USERS U JOIN PATIENTS P ON U.USER_ID = P.USER_ID " +
                    "WHERE U.USERNAME = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            Patient patient = null;
            if (rs.next()) {
                patient = extractPatientFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return patient;

        } catch (SQLException e) {
            System.err.println("Error getting patient by username: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get patient by patient ID (not user ID)
     * @param patientId Patient ID
     * @return Patient object, or null if not found
     * @throws SQLException if database error occurs
     */
    public Patient getByPatientId(int patientId) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "P.PATIENT_ID, P.DATE_OF_BIRTH, P.BLOOD_GROUP " +
                    "FROM USERS U JOIN PATIENTS P ON U.USER_ID = P.USER_ID " +
                    "WHERE P.PATIENT_ID = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            Patient patient = null;
            if (rs.next()) {
                patient = extractPatientFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return patient;

        } catch (SQLException e) {
            System.err.println("Error getting patient by patient ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to extract Patient object from ResultSet
     * @param rs ResultSet containing patient data
     * @return Patient object
     * @throws SQLException if database error occurs
     */
    private Patient extractPatientFromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("USER_ID");
        String username = rs.getString("USERNAME");
        String password = rs.getString("PASSWORD");
        String name = rs.getString("NAME");
        String email = rs.getString("EMAIL");
        String phone = rs.getString("PHONE");
        int patientId = rs.getInt("PATIENT_ID");

        Date dobDate = rs.getDate("DATE_OF_BIRTH");
        LocalDate dob = (dobDate != null) ? dobDate.toLocalDate() : null;

        String bloodGroup = rs.getString("BLOOD_GROUP");

        return new Patient(userId, username, password, name, email, phone,
                          patientId, dob, bloodGroup);
    }
}
