
package com.example.jobPortal.controller;

import com.example.jobPortal.dto.LoginRequestDTO;
import com.example.jobPortal.dto.UserResponseDTO;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.GlobalExceptionHandler;
import com.example.jobPortal.security.JwtService;
import com.example.jobPortal.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Test 1: Successful registration
    @Test
    void registerUser_withValidData_returnsCreated() throws Exception {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("satwik");
        savedUser.setEmail("satwik@example.com");
        savedUser.setRole("USER");

        when(userService.registerUser(any(User.class)))
                .thenReturn(savedUser);

        String json = """
                {
                    "username": "satwik",
                    "email": "satwik@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("satwik"))
                .andExpect(jsonPath("$.email").value("satwik@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).registerUser(any(User.class));
    }

    // Test 2: Invalid registration data
    @Test
    void registerUser_withInvalidData_returnsBadRequest() throws Exception {
        String json = """
                {
                    "username": "",
                    "email": "invalid-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    // Test 3: Duplicate email returns conflict
    @Test
    void registerUser_withDuplicateEmail_returnsConflict() throws Exception {
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException(
                        "Email is already registered"));

        String json = """
                {
                    "username": "satwik",
                    "email": "satwik@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email is already registered"));

        verify(userService).registerUser(any(User.class));
    }

    // Test 4: Successful login
    @Test
    void loginUser_withValidCredentials_returnsOk() throws Exception {
        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getName()).thenReturn("satwik");
        when(authentication.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtService.generateToken("satwik"))
                .thenReturn("test-jwt-token");

        String json = """
                {
                    "username": "satwik",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.username").value("satwik"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(header().exists("Set-Cookie"));

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken("satwik");
    }

    // Test 5: Wrong login credentials are rejected
    @Test
    void loginUser_withWrongCredentials_returnsUnauthorized()
            throws Exception {

        when(authenticationManager.authenticate(any(
                UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        String json = """
                {
                    "username": "satwik",
                    "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid username or password"));

        verify(authenticationManager).authenticate(any(
                UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }

    // Test 6: Logout clears the JWT cookie
    @Test
    void logoutUser_returnsOkAndClearsCookie() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Logged out successfully"))
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
