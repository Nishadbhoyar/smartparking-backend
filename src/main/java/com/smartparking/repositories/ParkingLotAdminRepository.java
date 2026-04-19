package com.smartparking.repositories;

import com.smartparking.entities.admins.ParkingLotAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ParkingLotAdminRepository extends JpaRepository<ParkingLotAdmin, Long> {}