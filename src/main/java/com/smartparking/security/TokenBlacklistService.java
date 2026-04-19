package com.smartparking.security;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * M-2 FIX: In-memory JWT blacklist.
 *
 * When a user logs out, their token is added here.
 * JwtAuthFilter checks this before trusting any token.
 *
 * Trade-off: entries are lost on server restart (acceptable for a single-instance app).
 * For multi-instance or persistent blacklisting, replace the Set with a Redis TTL key.
 */
@Service
public class TokenBlacklistService {

    private final Set<String> blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void blacklist(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
