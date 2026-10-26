package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite driver not found. Reload Maven (pom.xml) and try again.");
            return;
        }

        // 1. Initialize Database tables
        DatabaseInitializer.initializeDatabase();

        // 2. Instantiate DAOs & Utilities, and delegate execution to Authentication.start()
        try (Scanner sc = new Scanner(System.in)) {
            UserDao userDao = new UserDao();
            Authentication auth = new Authentication(sc, userDao);
            auth.start();
        }
    }
}
