
package com.example.jobPortal.dto;

public class JobResponseDTO {

    private Long id;
    private String title;
    private String company;
    private String location;
    private String description;
    private Long recruiterId;
    private String recruiterUsername;

    // Existing constructor: backward compatibility
    public JobResponseDTO(
            Long id,
            String title,
            String company,
            String location,
            Long recruiterId,
            String recruiterUsername) {

        this(id, title, company, location, null, recruiterId, recruiterUsername);
    }

    // New constructor with job description
    public JobResponseDTO(
            Long id,
            String title,
            String company,
            String location,
            String description,
            Long recruiterId,
            String recruiterUsername) {

        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
        this.recruiterId = recruiterId;
        this.recruiterUsername = recruiterUsername;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCompany() {
        return company;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public Long getRecruiterId() {
        return recruiterId;
    }

    public String getRecruiterUsername() {
        return recruiterUsername;
    }
}
