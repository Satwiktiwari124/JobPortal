package com.example.jobPortal.service;

import com.example.jobPortal.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.jobPortal.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.recruiter.registration-code:}")
    private String recruiterRegistrationCode;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Please log in to view your profile");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Transactional
    public User updateCurrentUserEmail(String email) {
        User user = getCurrentUser();
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(normalizedEmail, user.getId())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        user.setEmail(normalizedEmail);
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(User user) {

        return createUser(user, "USER");
    }

    @Transactional
    public User registerRecruiter(User user, String inviteCode) {
        if (recruiterRegistrationCode == null || recruiterRegistrationCode.isBlank()) {
            throw new IllegalArgumentException("Recruiter registration is currently disabled");
        }
        if (inviteCode == null || !java.security.MessageDigest.isEqual(
                recruiterRegistrationCode.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                inviteCode.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid recruiter invitation code");
        }
        return createUser(user, "RECRUITER");
    }

    private User createUser(User user, String role) {
        String email = user.getEmail().trim().toLowerCase();
        String username = user.getUsername().trim();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
