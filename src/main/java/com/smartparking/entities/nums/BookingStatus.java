package com.smartparking.entities.nums;

public enum BookingStatus {
    PENDING,   // Added for when a user books on their phone but hasn't arrived yet
    ACTIVE,    // They are currently parked in the lot
    COMPLETED, // They have left the lot
    CANCELLED
}