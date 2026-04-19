package com.smartparking.entities.nums;

public enum CarRentalStatus {
    PENDING,  // customer requested, owner hasn't confirmed
    CONFIRMED,  // owner confirmed, waiting for pickup
    ACTIVE,  // car is with customer right now
    COMPLETED, // car returned, booking done
    CANCELLED
}
