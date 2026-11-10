
package com.example.jobPortal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendApplicationConfirmation(
            String to,
            String candidateName,
            String jobTitle,
            String companyName,
            String location) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Job Application Successful - " + jobTitle);

        message.setText(
                "Hello " + candidateName + ",\n\n" +
                        "Your job application has been submitted successfully.\n\n" +
                        "Job Title: " + jobTitle + "\n" +
                        "Company: " + companyName + "\n" +
                        "Location: " + location + "\n\n" +
                        "Thank you for using our Job Portal!"
        );

        javaMailSender.send(message);
    }
}
