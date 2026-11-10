package com.example.jobPortal.config;

import com.example.jobPortal.dto.JobRequestDTO;
import com.example.jobPortal.dto.JobResponseDTO;
import com.example.jobPortal.security.JwtService;
import com.example.jobPortal.service.CustomUserDetailsService;
import com.example.jobPortal.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(jobService.getAllJobs()).thenReturn(java.util.List.of());
    }

    private void mockToken(String token, String username, String role) {
        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password("test-password")
                        .roles(role)
                        .build();

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(userDetails);
        when(jwtService.isTokenValid(token, username)).thenReturn(true);
    }

    private String validJobJson() {
        return """
                {
                    "title": "Java Developer",
                    "company": "ABC",
                    "location": "Noida",
                    "recruiterId": 1
                }
                """;
    }

    @Test
    void getJobs_withoutToken_isAllowed() throws Exception {
        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void createJob_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJobJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createJob_withCandidateToken_returnsForbidden() throws Exception {
        String token = "candidate-token";
        mockToken(token, "candidate", "USER");

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJobJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createJob_withRecruiterToken_isAuthorized() throws Exception {
        String token = "recruiter-token";
        mockToken(token, "recruiter", "RECRUITER");

        when(jobService.saveJob(any(JobRequestDTO.class)))
                .thenReturn(new JobResponseDTO(
                        1L,
                        "Java Developer",
                        "ABC",
                        "Noida",
                        1L,
                        "recruiter"
                ));

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJobJson()))
                .andExpect(status().isCreated());

        verify(jobService).saveJob(any(JobRequestDTO.class));
    }

    @Test
    void createJob_withInvalidToken_returnsUnauthorized() throws Exception {
        when(jwtService.extractUsername("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        mockMvc.perform(post("/jobs")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJobJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateJob_withCandidateToken_returnsForbidden() throws Exception {
        String token = "candidate-token";
        mockToken(token, "candidate", "USER");

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/jobs/1")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(validJobJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteJob_withCandidateToken_returnsForbidden() throws Exception {
        String token = "candidate-token";
        mockToken(token, "candidate", "USER");

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/jobs/1")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
