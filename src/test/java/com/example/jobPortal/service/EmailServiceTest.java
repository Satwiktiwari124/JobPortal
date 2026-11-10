
package com.example.jobPortal.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void sendTestEmail() {

        String recipient = System.getenv("MAIL_USERNAME");

        emailService.sendApplicationConfirmation(
                recipient,
                "Test Candidate",
                "Java Developer",
                "ABC Technologies",
                "Noida"
        );
    }
}
