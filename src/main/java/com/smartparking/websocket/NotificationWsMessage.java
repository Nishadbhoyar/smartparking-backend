package com.smartparking.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pushed to /topic/notifications/{userId} whenever a notification is saved.
 * Frontend receives this and can immediately show the badge / prepend to list
 * without waiting for the next poll cycle.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWsMessage {
    private Long   id;
    private String title;
    private String body;
    private String type;       // VALET | BOOKING | RENTAL | SYSTEM
    private String createdAt;  // ISO-8601 string — frontend uses timeAgo()
}