package projectcode1.models;

/**
 * Admin class - Represents an administrator user
 * Demonstrates: Inheritance (extends User), Polymorphism
 *
 * @author CS313 Term Project
 */
public class Admin extends User {
    // Admin-specific attributes
    private int adminId;

    /**
     * Default constructor
     */
    public Admin() {
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
     * @param adminId Admin ID
     */
    public Admin(int userId, String username, String password, String name,
                 String email, String phone, int adminId) {
        super(userId, username, password, "ADMIN", name, email, phone);
        this.adminId = adminId;
    }

    /**
     * Constructor without IDs (for new admins)
     * @param username Username
     * @param password Password
     * @param name Full name
     * @param email Email
     * @param phone Phone
     */
    public Admin(String username, String password, String name,
                 String email, String phone) {
        super(username, password, "ADMIN", name, email, phone);
    }

    // Implementing abstract methods from User class (Polymorphism)
    /**
     * Implementation of abstract getRole() method
     * @return "ADMIN"
     */
    @Override
    public String getRole() {
        return "ADMIN";
    }

    /**
     * Implementation of abstract displayInfo() method
     * Displays admin-specific information
     */
    @Override
    public void displayInfo() {
        System.out.println("=== Administrator Information ===");
        System.out.println("Admin ID: " + adminId);
        System.out.println("Name: " + getName());
        System.out.println("Email: " + getEmail());
        System.out.println("Phone: " + getPhone());
    }

    // Getters and Setters for admin-specific attributes
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    /**
     * Override toString() for debugging
     * @return String representation of Admin object
     */
    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", userId=" + getUserId() +
                ", name='" + getName() + '\'' +
                '}';
    }
}
