package com.smartparking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  // ✅ Required for @Cacheable in CustomUserDetailsService to work
public class SmartparkingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartparkingApplication.class, args);
    }
}