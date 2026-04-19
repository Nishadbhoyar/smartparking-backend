package com.smartparking.entities.admins;

import com.smartparking.entities.users.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "super_admins")
public class SuperAdmin extends User {
    // Has access to everything — no extra fields needed yet
    // Future: add audit log reference, 2FA flag, etc.
}