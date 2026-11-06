package com.scc.dao;

import com.scc.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {
    private final ActivityLogDao logDao = new ActivityLogDao();

    public boolean addBook(String title, String author, String category, int quantity, int userId) {
        String sql = "INSERT INTO books(title, author, category, quantity, availability) VALUES(?, ?, ?, ?, ?)";
        String availability = quantity > 0 ? "AVAILABLE" : "UNAVAILABLE";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, category);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, availability);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }

        if (success) {
            logDao.logAction(userId, "BOOK_CREATED", "Added new book title: " + title);
        }
        return success;
    }

    public boolean updateBook(int bookId, String title, String author, String category, int quantity, int userId) {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, quantity = ?, availability = ? WHERE book_id = ?";
        String availability = quantity > 0 ? "AVAILABLE" : "UNAVAILABLE";
        boolean success = false;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, category);
            pstmt.setInt(4, quantity);
            pstmt.setString(5, availability);
            pstmt.setInt(6, bookId);
            success = pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }

        if (success) {
            logDao.logAction(userId, "BOOK_UPDATED", "Updated book ID #" + bookId + " status to " + availability);
        }
        return success;
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author, category, quantity, availability FROM books";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getString("availability")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching books: " + e.getMessage());
        }
        return books;
    }
}