
package com.smartparking.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "valets") // 👈 Creates a separate 'valets' table
@PrimaryKeyJoinColumn(name = "valet_id") // Links to users table
public class Valet extends User {

    private String employeeId; // e.g., EMP-001
    private String shiftTiming; // e.g., "9AM - 5PM"
    private Long assignedLotId; // Which lot do they work at?

    public Valet() {
    }

    // Getters and Setters
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getShiftTiming() {
        return shiftTiming;
    }

    public void setShiftTiming(String shiftTiming) {
        this.shiftTiming = shiftTiming;
    }

    public Long getAssignedLotId() {
        return assignedLotId;
    }

    public void setAssignedLotId(Long assignedLotId) {
        this.assignedLotId = assignedLotId;
    }
}