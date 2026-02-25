package com.sms.domain;

import java.time.LocalDate;

/**
 * Represents a student record in the system.
 * This is the core domain model - it just holds data, no business logic here.
 */
public class Student {

    private String studentId;
    private String fullName;
    private String programme;
    private int level;
    private double gpa;
    private String email;
    private String phoneNumber;
    private LocalDate dateAdded;
    private String status; // "Active" or "Inactive"

    // Default constructor
    public Student() {
        this.dateAdded = LocalDate.now();
        this.status = "Active";
    }

    // Full constructor
    public Student(String studentId, String fullName, String programme,
                   int level, double gpa, String email, String phoneNumber,
                   LocalDate dateAdded, String status) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.programme = programme;
        this.level = level;
        this.gpa = gpa;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateAdded = dateAdded;
        this.status = status;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public String getStudentId()    { return studentId; }
    public String getFullName()     { return fullName; }
    public String getProgramme()    { return programme; }
    public int    getLevel()        { return level; }
    public double getGpa()          { return gpa; }
    public String getEmail()        { return email; }
    public String getPhoneNumber()  { return phoneNumber; }
    public LocalDate getDateAdded() { return dateAdded; }
    public String getStatus()       { return status; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setStudentId(String studentId)     { this.studentId = studentId; }
    public void setFullName(String fullName)        { this.fullName = fullName; }
    public void setProgramme(String programme)      { this.programme = programme; }
    public void setLevel(int level)                 { this.level = level; }
    public void setGpa(double gpa)                  { this.gpa = gpa; }
    public void setEmail(String email)              { this.email = email; }
    public void setPhoneNumber(String phoneNumber)  { this.phoneNumber = phoneNumber; }
    public void setDateAdded(LocalDate dateAdded)   { this.dateAdded = dateAdded; }
    public void setStatus(String status)            { this.status = status; }

    @Override
    public String toString() {
        return "Student{id='" + studentId + "', name='" + fullName + "', programme='" + programme + "'}";
    }
}
