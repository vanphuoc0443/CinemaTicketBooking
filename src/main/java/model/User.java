package model;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Model User để authentication (kế thừa từ Customer)
 * Thêm các trường password, salt, lastLogin
 */
public class User extends Customer {
    private String passwordHash;
    private String salt;
    private Timestamp lastLogin;
    private boolean isActive;
    private String sessionToken;

    public User() {
        super();
        this.isActive = true;
    }

    public User(int customerId, String name, String email, String phone) {
        super(customerId, name, email, phone);
        this.isActive = true;
    }

    public User(Customer customer) {
        super(customer.getCustomerId(), customer.getName(),
                customer.getEmail(), customer.getPhone(),
                customer.getDateOfBirth());
        this.setCreatedAt(customer.getCreatedAt());
        this.setUpdatedAt(customer.getUpdatedAt());
        this.isActive = true;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "customerId=" + getCustomerId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", isActive=" + isActive +
                ", lastLogin=" + lastLogin +
                '}';
    }
}