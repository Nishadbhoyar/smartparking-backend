package com.smartparking.repositories;

import com.smartparking.entities.admins.CarOwner;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CarOwnerRepository extends JpaRepository<CarOwner, Long> {}