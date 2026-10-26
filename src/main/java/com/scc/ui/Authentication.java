package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Optional;
import java.util.Scanner;

public class Authentication {
    private static final String[] ROLES = {"ADMIN", "LIBRARY_STAFF", "STUDENT_MEMBER", "FACULTY"};

    private final Scanner sc;
    private final UserDao userDao;

    public Authentication(Scanner sc, UserDao userDao) {
        this.sc = sc;
        this.userDao = userDao;
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

        Optional<User> result = userDao.login(username, password);
        if (result.isPresent()) {
            User user = result.get();
            System.out.println("Login successful! Account status: " + user.status());

            // Route ADMIN to AdminMenu, others to UserMenu
            if (user.roles().equalsIgnoreCase("ADMIN")) {
                new AdminMenu(sc, user, userDao).show();
            } else {
                new UserMenu(sc, user, userDao).show();
            }
        } else {
            System.out.println("Invalid username or password.");
        }
    }
}