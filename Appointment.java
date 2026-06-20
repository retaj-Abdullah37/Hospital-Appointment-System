package projectcode1.dao;

import projectcode1.models.Doctor;
import java.sql.*;
import java.util.ArrayList;

/**
 * DoctorDAO class - Data Access Object for Doctor entity
 * Demonstrates: Interface implementation, Exception Handling, Collections
 *
 * Implements Manageable<Doctor> interface for CRUD operations
 *
 * @author CS313 Term Project
 */
public class DoctorDAO implements Manageable<Doctor> {

    private Connection connection;

    /**
     * Constructor - gets database connection
     */
    public DoctorDAO() {
        try {
            this.connection = DatabaseConnection.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection in DoctorDAO!");
            e.printStackTrace();
        }
    }

    /**
     * Add a new doctor to the database
     * @param doctor Doctor object to add
     * @throws SQLException if database error occurs
     */
    @Override
    public void add(Doctor doctor) throws SQLException {
        String userSql = "INSERT INTO USERS (USER_ID, USERNAME, PASSWORD, ROLE, NAME, EMAIL, PHONE) " +
                        "VALUES (USER_SEQ.NEXTVAL, ?, ?, 'DOCTOR', ?, ?, ?)";
        String doctorSql = "INSERT INTO DOCTORS (DOCTOR_ID, USER_ID, SPECIALTY, EXPERIENCE_YEARS) " +
                          "VALUES (DOCTOR_SEQ.NEXTVAL, USER_SEQ.CURRVAL, ?, ?)";

        try {
            // Insert into USERS table
            PreparedStatement userStmt = connection.prepareStatement(userSql);
            userStmt.setString(1, doctor.getUsername());
            userStmt.setString(2, doctor.getPassword());
            userStmt.setString(3, doctor.getName());
            userStmt.setString(4, doctor.getEmail());
            userStmt.setString(5, doctor.getPhone());
            userStmt.executeUpdate();
            userStmt.close();

            // Insert into DOCTORS table
            PreparedStatement doctorStmt = connection.prepareStatement(doctorSql);
            doctorStmt.setString(1, doctor.getSpecialty());
            doctorStmt.setInt(2, doctor.getExperienceYears());
            doctorStmt.executeUpdate();
            doctorStmt.close();

            System.out.println("Doctor added successfully!");

        } catch (SQLException e) {
            System.err.println("Error adding doctor: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Update an existing doctor in the database
     * @param doctor Doctor object with updated information
     * @throws SQLException if database error occurs
     */
    @Override
    public void update(Doctor doctor) throws SQLException {
        String userSql = "UPDATE USERS SET USERNAME=?, PASSWORD=?, NAME=?, EMAIL=?, PHONE=? " +
                        "WHERE USER_ID=?";
        String doctorSql = "UPDATE DOCTORS SET SPECIALTY=?, EXPERIENCE_YEARS=? " +
                          "WHERE USER_ID=?";

        try {
            // Update USERS table
            PreparedStatement userStmt = connection.prepareStatement(userSql);
            userStmt.setString(1, doctor.getUsername());
            userStmt.setString(2, doctor.getPassword());
            userStmt.setString(3, doctor.getName());
            userStmt.setString(4, doctor.getEmail());
            userStmt.setString(5, doctor.getPhone());
            userStmt.setInt(6, doctor.getUserId());
            userStmt.executeUpdate();
            userStmt.close();

            // Update DOCTORS table
            PreparedStatement doctorStmt = connection.prepareStatement(doctorSql);
            doctorStmt.setString(1, doctor.getSpecialty());
            doctorStmt.setInt(2, doctor.getExperienceYears());
            doctorStmt.setInt(3, doctor.getUserId());
            doctorStmt.executeUpdate();
            doctorStmt.close();

            System.out.println("Doctor updated successfully!");

        } catch (SQLException e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a doctor by ID
     * @param userId User ID of the doctor to delete
     * @throws SQLException if database error occurs
     */
    @Override
    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM USERS WHERE USER_ID=? AND ROLE='DOCTOR'";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            stmt.close();

            if (rowsAffected > 0) {
                System.out.println("Doctor deleted successfully!");
            } else {
                System.out.println("Doctor not found!");
            }

        } catch (SQLException e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get a doctor by user ID
     * @param userId User ID of the doctor
     * @return Doctor object, or null if not found
     * @throws SQLException if database error occurs
     */
    @Override
    public Doctor getById(int userId) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                    "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                    "WHERE U.USER_ID = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            Doctor doctor = null;
            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return doctor;

        } catch (SQLException e) {
            System.err.println("Error getting doctor by ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get all doctors from the database
     * @return ArrayList of Doctor objects
     * @throws SQLException if database error occurs
     */
    @Override
    public ArrayList<Doctor> getAll() throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                    "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                    "ORDER BY U.NAME";

        ArrayList<Doctor> doctors = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Doctor doctor = extractDoctorFromResultSet(rs);
                doctors.add(doctor);
            }

            rs.close();
            stmt.close();
            return doctors;

        } catch (SQLException e) {
            System.err.println("Error getting all doctors: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get doctor by username
     * @param username Username to search for
     * @return Doctor object, or null if not found
     * @throws SQLException if database error occurs
     */
    public Doctor getByUsername(String username) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                    "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                    "WHERE U.USERNAME = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            Doctor doctor = null;
            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return doctor;

        } catch (SQLException e) {
            System.err.println("Error getting doctor by username: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get doctor by doctor ID (not user ID)
     * @param doctorId Doctor ID
     * @return Doctor object, or null if not found
     * @throws SQLException if database error occurs
     */
    public Doctor getByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                    "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                    "WHERE D.DOCTOR_ID = ?";

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            Doctor doctor = null;
            if (rs.next()) {
                doctor = extractDoctorFromResultSet(rs);
            }

            rs.close();
            stmt.close();
            return doctor;

        } catch (SQLException e) {
            System.err.println("Error getting doctor by doctor ID: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get doctors by specialty
     * @param specialty Specialty to filter by
     * @return ArrayList of Doctor objects
     * @throws SQLException if database error occurs
     */
    public ArrayList<Doctor> getBySpecialty(String specialty) throws SQLException {
        String sql = "SELECT U.USER_ID, U.USERNAME, U.PASSWORD, U.NAME, U.EMAIL, U.PHONE, " +
                    "D.DOCTOR_ID, D.SPECIALTY, D.EXPERIENCE_YEARS " +
                    "FROM USERS U JOIN DOCTORS D ON U.USER_ID = D.USER_ID " +
                    "WHERE D.SPECIALTY = ? " +
                    "ORDER BY U.NAME";

        ArrayList<Doctor> doctors = new ArrayList<>();

        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, specialty);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Doctor doctor = extractDoctorFromResultSet(rs);
                doctors.add(doctor);
            }

            rs.close();
            stmt.close();
            return doctors;

        } catch (SQLException e) {
            System.err.println("Error getting doctors by specialty: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get all distinct specialties
     * @return ArrayList of specialty strings
     * @throws SQLException if database error occurs
     */
    public ArrayList<String> getAllSpecialties() throws SQLException {
        String sql = "SELECT DISTINCT SPECIALTY FROM DOCTORS ORDER BY SPECIALTY";
        ArrayList<String> specialties = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                specialties.add(rs.getString("SPECIALTY"));
            }

            rs.close();
            stmt.close();
            return specialties;

        } catch (SQLException e) {
            System.err.println("Error getting specialties: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to extract Doctor object from ResultSet
     * @param rs ResultSet containing doctor data
     * @return Doctor object
     * @throws SQLException if database error occurs
     */
    private Doctor extractDoctorFromResultSet(ResultSet rs) throws SQLException {
        int userId = rs.getInt("USER_ID");
        String username = rs.getString("USERNAME");
        String password = rs.getString("PASSWORD");
        String name = rs.getString("NAME");
        String email = rs.getString("EMAIL");
        String phone = rs.getString("PHONE");
        int doctorId = rs.getInt("DOCTOR_ID");
        String specialty = rs.getString("SPECIALTY");
        int experienceYears = rs.getInt("EXPERIENCE_YEARS");

        return new Doctor(userId, username, password, name, email, phone,
                         doctorId, specialty, experienceYears);
    }
}
