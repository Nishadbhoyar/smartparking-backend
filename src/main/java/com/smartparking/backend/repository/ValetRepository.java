package com.smartparking.backend.repository;

import com.smartparking.backend.model.Valet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ValetRepository extends JpaRepository<Valet, Long> {
    // ✅ Custom query method - Spring Data JPA automatically implements this
    Valet findByEmail(String email);
}