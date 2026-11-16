package com.example.jobPortal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String apiKey;

    @Value("${MAIL_FROM}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void sendApplicationConfirmation(
            String to,
            String candidateName,
            String jobTitle,
            String companyName,
            String location) {

        String subject = "Job Application Successful - " + jobTitle;

        String text = "Hello " + candidateName + ",\n\n" +
                "Your job application has been submitted successfully.\n\n" +
                "Job Title: " + jobTitle + "\n" +
                "Company: " + companyName + "\n" +
                "Location: " + location + "\n\n" +
                "Thank you for using our Job Portal!";

        sendEmail(to, subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = Map.of(
                    "sender", Map.of("name", "JobPortal", "email", fromEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", text
            );

            restTemplate.postForEntity(
                    "https://api.brevo.com/v3/smtp/email",
                    new HttpEntity<>(body, headers),
                    String.class
            );

            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Email failed: " + e.getMessage());
        }
    }
}