package com.example.jobPortal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Job {

        @Id
        @GeneratedValue
        private Long id;

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title is too long")
        private String title;

        @NotBlank(message = "Company is required")
        @Size(max = 150, message = "Company name is too long")
        private String company;

        @NotBlank(message = "Location is required")
        @Size(max = 150, message = "Location is too long")
        private String location;

        @Column(columnDefinition = "TEXT")
        @Size(max = 10000, message = "Description is too long")
        private String description;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User recruiter;
}