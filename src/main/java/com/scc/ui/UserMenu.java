package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.Scanner;

public class UserMenu {
    private final Scanner sc;
    private final User user;
    private final UserDao userDao;

    public UserMenu(Scanner sc, User user, UserDao userDao) {
        this.sc = sc;
        this.user = user;
        this.userDao = userDao;
    }

    public void show() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- USER MENU (" + user.roles() + ") ---");
            System.out.println("Logged in as: " + user);
            System.out.println("1. Logout");
            System.out.print("Choose an option: ");

            if (sc.nextLine().trim().equals("1")) {
                System.out.println("You have been logged out.");
                loggedIn = false;
            } else {
                System.out.println("Invalid option. Try again.");
            }
        }
    }
}