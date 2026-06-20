package projectcode1.models;

import java.time.LocalDate;

/**
 * Appointment class - Represents an appointment entity
 * Demonstrates: Encapsulation, Composition (has-a relationship with Patient and Doctor)
 *
 * @author CS313 Term Project
 */
public class Appointment {
    // Private attributes - Encapsulation
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private String status; // SCHEDULED, COMPLETED, CANCELLED
    private String visitNotes;

    // Composition - Appointment contains Patient and Doctor objects
    private Patient patient;
    private Doctor doctor;

    // Additional fields for display in TableView
    private String doctorName;
    private String specialty;
    private String patientName;
    private String patientPhone;

    /**
     * Default constructor
     */
    public Appointment() {
    }

    /**
     * Full parameterized constructor
     * @param appointmentId Appointment ID
     * @param patientId Patient ID
     * @param doctorId Doctor ID
     * @param appointmentDate Date of appointment
     * @param appointmentTime Time of appointment
     * @param status Appointment status
     * @param visitNotes Visit notes
     */
    public Appointment(int appointmentId, int patientId, int doctorId,
                      LocalDate appointmentDate, String appointmentTime,
                      String status, String visitNotes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.visitNotes = visitNotes;
    }

    /**
     * Constructor without appointment ID (for new appointments)
     * @param patientId Patient ID
     * @param doctorId Doctor ID
     * @param appointmentDate Date of appointment
     * @param appointmentTime Time of appointment
     * @param status Appointment status
     */
    public Appointment(int patientId, int doctorId, LocalDate appointmentDate,
                      String appointmentTime, String status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
    }

    /**
     * Constructor with Patient and Doctor objects (Composition)
     * @param patient Patient object
     * @param doctor Doctor object
     * @param appointmentDate Date of appointment
     * @param appointmentTime Time of appointment
     */
    public Appointment(Patient patient, Doctor doctor, LocalDate appointmentDate,
                      String appointmentTime) {
        this.patient = patient;
        this.doctor = doctor;
        this.patientId = patient.getPatientId();
        this.doctorId = doctor.getDoctorId();
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "SCHEDULED";
    }

    // Getters and Setters
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisitNotes() {
        return visitNotes;
    }

    public void setVisitNotes(String visitNotes) {
        this.visitNotes = visitNotes;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        this.patientId = patient.getPatientId();
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
        this.doctorId = doctor.getDoctorId();
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    /**
     * Method to mark appointment as completed
     * @param notes Visit notes from the doctor
     */
    public void markCompleted(String notes) {
        this.status = "COMPLETED";
        this.visitNotes = notes;
    }

    /**
     * Method to cancel appointment
     */
    public void cancel() {
        this.status = "CANCELLED";
    }

    /**
     * Check if appointment is scheduled
     * @return true if scheduled, false otherwise
     */
    public boolean isScheduled() {
        return "SCHEDULED".equals(this.status);
    }

    /**
     * Check if appointment is completed
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    /**
     * Check if appointment is cancelled
     * @return true if cancelled, false otherwise
     */
    public boolean isCancelled() {
        return "CANCELLED".equals(this.status);
    }

    /**
     * Override toString() for debugging
     * @return String representation of Appointment object
     */
    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentDate=" + appointmentDate +
                ", appointmentTime='" + appointmentTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
