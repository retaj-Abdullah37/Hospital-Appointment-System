package projectcode1.models;

/**
 * Doctor class - Represents a doctor user
 * Demonstrates: Inheritance (extends User), Polymorphism
 *
 * @author CS313 Term Project
 */
public class Doctor extends User {
    // Doctor-specific attributes
    private int doctorId;
    private String specialty;
    private int experienceYears;

    /**
     * Default constructor
     */
    public Doctor() {
        super();
    }

    /**
     * Full parameterized constructor
     * @param userId User ID
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     * @param phone Phone
     * @param doctorId Doctor ID
     * @param specialty Medical specialty
     * @param experienceYears Years of experience
     */
    public Doctor(int userId, String username, String password, String name,
                  String email, String phone, int doctorId,
                  String specialty, int experienceYears) {
        super(userId, username, password, "DOCTOR", name, email, phone);
        this.doctorId = doctorId;
        this.specialty = specialty;
        this.experienceYears = experienceYears;
    }

    /**
     * Constructor without IDs (for new doctors)
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     * @param phone Phone
     * @param specialty Medical specialty
     * @param experienceYears Years of experience
     */
    public Doctor(String username, String password, String name,
                  String email, String phone, String specialty,
                  int experienceYears) {
        super(username, password, "DOCTOR", name, email, phone);
        this.specialty = specialty;
        this.experienceYears = experienceYears;
    }

    // Implementing abstract methods from User class (Polymorphism)
    /**
     * Implementation of abstract getRole() method
     * @return "DOCTOR"
     */
    @Override
    public String getRole() {
        return "DOCTOR";
    }

    /**
     * Implementation of abstract displayInfo() method
     * Displays doctor-specific information
     */
    @Override
    public void displayInfo() {
        System.out.println("=== Doctor Information ===");
        System.out.println("Doctor ID: " + doctorId);
        System.out.println("Name: " + getName());
        System.out.println("Email: " + getEmail());
        System.out.println("Phone: " + getPhone());
        System.out.println("Specialty: " + specialty);
        System.out.println("Experience: " + experienceYears + " years");
    }

    // Getters and Setters for doctor-specific attributes
    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    /**
     * Override toString() for debugging
     * @return String representation of Doctor object
     */
    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", userId=" + getUserId() +
                ", name='" + getName() + '\'' +
                ", specialty='" + specialty + '\'' +
                ", experienceYears=" + experienceYears +
                '}';
    }
}
