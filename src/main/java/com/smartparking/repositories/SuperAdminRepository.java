package com.smartparking.repositories;

import com.smartparking.entities.admins.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SuperAdminRepository extends JpaRepository<SuperAdmin, Long> {}