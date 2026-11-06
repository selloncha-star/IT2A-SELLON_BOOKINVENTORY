package com.scc.model;

public record ActivityLog(int id, int userId, String actionType, String description, String timestamp) {
    @Override
    public String toString() {
        return String.format("[%d] User ID: %d | %s - %s (%s)", id, userId, actionType, description, timestamp);
    }
}