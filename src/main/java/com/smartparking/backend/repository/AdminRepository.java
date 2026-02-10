package com.smartparking.backend.repository;

import com.smartparking.backend.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    // ✅ Custom query method - Spring Data JPA automatically implements this
    Admin findByEmail(String email);
}