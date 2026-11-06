package com.scc.model;

public record Book(int bookId, String title, String author, String category, int quantity, String availability) {
    @Override
    public String toString() {
        return String.format("[%d] %s by %s (%s) - Qty: %d [%s]", bookId, title, author, category, quantity, availability);
    }
}