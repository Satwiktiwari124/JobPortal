package com.example.jobPortal.controller;

import java.time.Duration;
import java.util.Map;

import com.example.jobPortal.dto.LoginRequestDTO;
import com.example.jobPortal.dto.ProfileUpdateRequestDTO;
import com.example.jobPortal.dto.UserResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.security.JwtService;
import com.example.jobPortal.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    public UserController(
            UserService userService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(
            @Valid @RequestBody User user) {

        User savedUser = userService.registerUser(user);

        UserResponseDTO response = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @Valid @RequestBody LoginRequestDTO loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUsername(),
                                loginRequest.getPassword()
                        )
                );

        String token = jwtService.generateToken(
                authentication.getName()
        );

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_USER");

        ResponseCookie cookie = ResponseCookie.from("jwtToken", token)
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "message", "Login successful",
                        "username", authentication.getName(),
                        "role", role,
                        "token", token
                ));
    }

    @PostMapping("/register/recruiter")
    public ResponseEntity<UserResponseDTO> registerRecruiter(
            @Valid @RequestBody User user,
            @RequestHeader(value = "X-Recruiter-Invite-Code", required = false) String inviteCode) {
        User savedUser = userService.registerRecruiter(user, inviteCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @Valid @RequestBody ProfileUpdateRequestDTO request) {
        User user = userService.updateCurrentUserEmail(request.getEmail());
        return ResponseEntity.ok(new UserResponseDTO(
                user.getId(), user.getUsername(), user.getEmail(), user.getRole()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {

        ResponseCookie cookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(secureCookie)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
}
