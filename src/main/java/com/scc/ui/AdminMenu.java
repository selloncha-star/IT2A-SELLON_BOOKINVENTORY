package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private static final String[] ROLES = {"ADMIN", "LIBRARY_STAFF", "STUDENT_MEMBER", "FACULTY"};

    private final Scanner sc;
    private final User adminUser;
    private final UserDao userDao;

    public AdminMenu(Scanner sc, User adminUser, UserDao userDao) {
        this.sc = sc;
        this.adminUser = adminUser;
        this.userDao = userDao;
    }

    public void show() {
        boolean inAdminMenu = true;
        while (inAdminMenu) {
            System.out.println("\n========================================");
            System.out.println("         ADMIN DASHBOARD");
            System.out.println("========================================");
            System.out.println("Logged in as: " + adminUser.username() + " (ADMIN)");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending User Approvals");
            System.out.println("3. Approve User Registration");
            System.out.println("4. Add New User");
            System.out.println("5. Update User");
            System.out.println("6. Delete User");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");

            String choiceStr = sc.nextLine().trim();
            switch (choiceStr) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUser();
                case "4" -> addNewUser();
                case "5" -> updateUser();
                case "6" -> deleteUser();
                case "7" -> {
                    System.out.println("Logged out from Admin Dashboard.");
                    inAdminMenu = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private void printUserTable(List<User> users, String title) {
        System.out.println("\n--- " + title + " ---");
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println("+-----+------------------+------------------------+------------+");
        System.out.printf("| %-3s | %-16s | %-22s | %-10s |\n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("+-----+------------------+------------------------+------------+");

        for (User u : users) {
            System.out.printf("| %-3d | %-16s | %-22s | %-10s |\n",
                    u.id(),
                    u.username(),
                    u.roles(),
                    u.status());
        }
        System.out.println("+-----+------------------+------------------------+------------+");
    }

    private void viewAllUsers() {
        List<User> users = userDao.getAllUsers();
        printUserTable(users, "ALL USERS");
    }

    private void viewPendingUsers() {
        List<User> pending = userDao.getPendingUsers();
        printUserTable(pending, "PENDING USER APPROVALS");
    }

    private void approveUser() {
        System.out.println("\n--- APPROVE USER REGISTRATION ---");
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending user approvals found.");
            return;
        }

        System.out.println("+-----+------------------+------------------------+------------+");
        System.out.printf("| %-3s | %-16s | %-22s | %-10s |\n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("+-----+------------------+------------------------+------------+");
        for (User u : pending) {
            System.out.printf("| %-3d | %-16s | %-22s | %-10s |\n", u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+-----+------------------+------------------------+------------+");

        int id = readIntInput("Enter Target User ID to approve (Enter 0 to cancel): ");
        if (id == 0) {
            System.out.println("Approval action cancelled.");
            return;
        }

        boolean exists = pending.stream().anyMatch(u -> u.id() == id);
        if (!exists) {
            System.out.println("Error: Invalid User ID or user is not in the pending list.");
            return;
        }

        if (userDao.approveUser(id)) {
            System.out.println("-> User successfully approved and status updated to APPROVED!");
        } else {
            System.out.println("Failed to approve user.");
        }
    }

    private void addNewUser() {
        System.out.println("\n--- ADD NEW USER ---");
        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();

        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        String role = readValidRole();
        if (role == null) {
            System.out.println("Action cancelled.");
            return;
        }

        System.out.print("Set Status (e.g., APPROVED, PENDING): ");
        String status = sc.nextLine().trim().toUpperCase();
        if (status.isEmpty()) status = "APPROVED";

        if (userDao.adminAddUser(username, password, role, status)) {
            System.out.println("User added successfully!");
        } else {
            System.out.println("Failed to add user.");
        }
    }

    private void updateUser() {
        System.out.println("\n--- SELECT USER TO UPDATE ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println("+-----+------------------+------------------------+------------+");
        System.out.printf("| %-3s | %-16s | %-22s | %-10s |\n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("+-----+------------------+------------------------+------------+");
        for (User u : users) {
            System.out.printf("| %-3d | %-16s | %-22s | %-10s |\n", u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+-----+------------------+------------------------+------------+");

        int id = readIntInput("Enter User ID to update (Enter 0 to cancel): ");
        if (id == 0) {
            System.out.println("Update action cancelled.");
            return;
        }

        // Self-Edit Restriction Guardrail
        if (id == adminUser.id()) {
            System.out.println("WARNING: Modifying the currently active admin account is restricted.");
            return;
        }

        boolean exists = users.stream().anyMatch(u -> u.id() == id);
        if (!exists) {
            System.out.println("Error: User ID not found.");
            return;
        }

        String newRole = readValidRole();
        if (newRole == null) {
            System.out.println("Update action cancelled.");
            return;
        }

        String newStatus = readValidStatus();
        if (newStatus == null) {
            System.out.println("Update action cancelled.");
            return;
        }

        if (userDao.updateUserRoleAndStatus(id, newRole, newStatus)) {
            System.out.println("-> User role and status updated successfully!");
        } else {
            System.out.println("Failed to update user.");
        }
    }

    private void deleteUser() {
        System.out.println("\n--- DELETE USER ---");
        List<User> users = userDao.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No records found.");
            return;
        }

        System.out.println("+-----+------------------+------------------------+------------+");
        System.out.printf("| %-3s | %-16s | %-22s | %-10s |\n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("+-----+------------------+------------------------+------------+");
        for (User u : users) {
            System.out.printf("| %-3d | %-16s | %-22s | %-10s |\n", u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("+-----+------------------+------------------------+------------+");

        int id = readIntInput("Enter User ID to delete (Enter 0 to cancel): ");
        if (id == 0) {
            System.out.println("Deletion action cancelled.");
            return;
        }

        if (id == adminUser.id()) {
            System.out.println("WARNING: Deleting the currently active admin account is restricted.");
            return;
        }

        if (userDao.deleteUser(id)) {
            System.out.println("-> User deleted successfully!");
        } else {
            System.out.println("Failed to delete user. Check ID.");
        }
    }

    // --- Input Validation Wrappers ---

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer number.");
            }
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.println("Select Role:");
            for (int i = 0; i < ROLES.length; i++) {
                System.out.printf("  [%d] %s\n", i + 1, ROLES[i]);
            }
            System.out.print("Role choice (Enter 0 to cancel): ");
            String input = sc.nextLine().trim();
            if (input.equals("0")) {
                return null;
            }
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= ROLES.length) {
                    return ROLES[choice - 1];
                }
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid role selection! Please choose a valid number from the list.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Enter status (APPROVED / PENDING) (Enter 0 to cancel): ");
            String status = sc.nextLine().trim().toUpperCase();
            if (status.equals("0")) {
                return null;
            }
            if (status.equals("APPROVED") || status.equals("PENDING")) {
                return status;
            }
            System.out.println("Invalid status! Must be either APPROVED or PENDING.");
        }
    }
}