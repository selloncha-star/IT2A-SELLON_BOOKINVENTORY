package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.dao.UserDao;
import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;
import com.scc.dao.DatabaseConnection;

public class Authentication {
    private static final String[] ROLES = {"ADMIN", "LIBRARY_STAFF", "STUDENT_MEMBER", "FACULTY"};

    private final Scanner sc;
    private final UserDao userDao;
    private final ActivityLogDao logDao;

    public Authentication(Scanner sc, UserDao userDao) {
        this.sc = sc;
        this.userDao = userDao;
        this.logDao = new ActivityLogDao();
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n========================================");
            System.out.println(" BOOK INVENTORY AND CATALOGING SYSTEM");
            System.out.println("========================================");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            switch (sc.nextLine().trim()) {
                case "1" -> login();
                case "2" -> register();
                case "3" -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Enter new username: ");
        String username = sc.nextLine().trim();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        System.out.println("Select Role:");
        for (int i = 0; i < ROLES.length; i++) {
            System.out.printf("  [%d] %s\n", i + 1, ROLES[i]);
        }
        System.out.print("Role: ");

        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            choice = -1;
        }
        if (choice < 1 || choice > ROLES.length) {
            System.out.println("Invalid role selection!");
            return;
        }

        if (userDao.registerUser(username, password, ROLES[choice - 1])) {
            System.out.println("-> Successful Registration, Waiting for Approval! (Status: PENDING)");
        }
    }

    private void login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine();

        // 1. Una, i-check nato kung ang account nag exist ba ug unsay status niini
        String sql = "SELECT status FROM users WHERE username = ? AND password = ?";
        String dbStatus = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dbStatus = rs.getString("status");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during login check: " + e.getMessage());
        }

        // 2. Kung ang status kay PENDING, i-display ang espesyal nga mensahe ug AYAW i-print ang "Invalid username or password"
        if (dbStatus != null && "PENDING".equalsIgnoreCase(dbStatus)) {
            System.out.println("Login failed: Your account is still PENDING approval by the admin.");
            return;
        }

        // 3. Kung APPROVED na o sakto ang kredensyal, padayon sa normal nga login method
        Optional<User> result = userDao.login(username, password);
        if (result.isPresent()) {
            User user = result.get();
            System.out.println("Login successful! Account status: " + user.status());

            if (user.roles().equalsIgnoreCase("ADMIN")) {
                new AdminMenu(sc, user, userDao).show();
            } else {
                new UserMenu(sc, user, userDao).show();
                logDao.logAction(user.id(), "USER_LOGOUT", "User " + user.username() + " (ID: " + user.id() + ") logged out.");
            }
        } else {
            // Kini mo-gawas lang kung tinuod nga sayop ang username o password
            System.out.println("Invalid username or password.");
        }
    }
}