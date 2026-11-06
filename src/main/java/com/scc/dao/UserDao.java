package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking username existence: " + e.getMessage());
            return false;
        }
    }

    public boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, 'PENDING')";
        int newUserId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newUserId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                System.out.println("Username already exists. Choose another one.");
            } else {
                System.err.println("Failed: " + e.getMessage());
            }
            return false;
        }

        if (newUserId != -1) {
            logDao.logAction(newUserId, "USER_REGISTERED", "User " + username + " registered successfully.");
        }
        return true;
    }

    public boolean adminAddUser(String username, String password, String role, String status, int adminId) {
        String sql = "INSERT INTO users(username, password, roles, status) VALUES(?, ?, ?, ?)";
        int newUserId = -1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role.toUpperCase());
            pstmt.setString(4, status.toUpperCase());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    newUserId = generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                System.out.println("Username already exists. Choose another one.");
            } else {
                System.err.println("Failed: " + e.getMessage());
            }
            return false;
        }

        if (newUserId != -1) {
            logDao.logAction(adminId, "ADMIN_ADD_USER", "Admin added user " + username + " with role " + role.toUpperCase());
        }
        return true;
    }

    public Optional<User> login(String username, String password) {
        // Gikuha nato ang status kauban ang username ug password sa usa ka query
        String sql = "SELECT id, username, password, roles, status FROM users WHERE username = ? AND password = ?";
        User foundUser = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundUser = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        if (foundUser != null) {
            if ("PENDING".equalsIgnoreCase(foundUser.status())) {
                System.out.println("Login failed: Your account is still PENDING.");
                return Optional.empty();
            }

            logDao.logAction(foundUser.id(), "USER_LOGIN", "User " + foundUser.username() + " (ID: " + foundUser.id() + ") logged in.");
            return Optional.of(foundUser);
        }

        return Optional.empty();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users: " + e.getMessage());
        }
        return users;
    }

    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, roles, status FROM users WHERE UPPER(status) = 'PENDING'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching pending users: " + e.getMessage());
        }
        return users;
    }

    public boolean approveUser(int userId, int adminId) {
        String sql = "UPDATE users SET status = 'APPROVED' WHERE id = ?";
        boolean updated = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            updated = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error approving user: " + e.getMessage());
            return false;
        }

        if (updated) {
            logDao.logAction(adminId, "USER_APPROVED", "Admin ID " + adminId + " approved account for User ID " + userId);
        }
        return updated;
    }

    public boolean updateUserRoleAndStatus(int userId, String newRole, String newStatus, int adminId) {
        String sql = "UPDATE users SET roles = ?, status = ? WHERE id = ?";
        boolean updated = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.toUpperCase());
            pstmt.setString(2, newStatus.toUpperCase());
            pstmt.setInt(3, userId);
            updated = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role and status: " + e.getMessage());
            return false;
        }

        if (updated) {
            logDao.logAction(adminId, "USER_UPDATED", "Admin ID " + adminId + " changed role of User ID " + userId + " to " + newRole.toUpperCase());
        }
        return updated;
    }

    public boolean deleteUser(int id, int adminId) {
        String sql = "DELETE FROM users WHERE id = ?";
        boolean deleted = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            deleted = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }

        if (deleted) {
            logDao.logAction(adminId, "USER_DELETED", "Admin ID " + adminId + " deleted User ID " + id);
        }
        return deleted;
    }
}