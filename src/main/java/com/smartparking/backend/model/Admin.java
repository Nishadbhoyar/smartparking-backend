package com.smartparking.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "admins") // 👈 Creates a separate 'admins' table
@PrimaryKeyJoinColumn(name = "admin_id") // Links to users table
public class Admin extends User {

    private String companyName;
    private String businessLicense;

    public Admin() {
    }

    // Getters and Setters for specific data
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }
}