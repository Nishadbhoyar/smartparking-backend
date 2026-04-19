package com.smartparking.repositories;

import com.smartparking.entities.admins.FleetAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FleetAdminRepository extends JpaRepository<FleetAdmin, Long> {}