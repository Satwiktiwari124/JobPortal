
package com.example.jobPortal.controller;

import com.example.jobPortal.dto.JobRequestDTO;
import com.example.jobPortal.dto.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.exception.GlobalExceptionHandler;
import com.example.jobPortal.exception.JobNotFoundException;
import com.example.jobPortal.service.JobService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(jobController)
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Test 1: Get all jobs
    @Test
    void getAllJobs_returnsOk() throws Exception {

        Job job = new Job();
        job.setId(1L);
        job.setTitle("Java Developer");
        job.setCompany("ABC Company");
        job.setLocation("Noida");

        when(jobService.getAllJobs())
                .thenReturn(List.of(job));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Java Developer"))
                .andExpect(jsonPath("$[0].company")
                        .value("ABC Company"))
                .andExpect(jsonPath("$[0].location")
                        .value("Noida"));

        verify(jobService).getAllJobs();
    }

    // Test 2: Get job by ID
    @Test
    void getJobById_whenJobExists_returnsOk() throws Exception {

        Job job = new Job();
        job.setId(1L);
        job.setTitle("Java Developer");
        job.setCompany("ABC Company");
        job.setLocation("Noida");

        when(jobService.getJobById(1L))
                .thenReturn(job);

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Java Developer"))
                .andExpect(jsonPath("$.company")
                        .value("ABC Company"))
                .andExpect(jsonPath("$.location")
                        .value("Noida"));

        verify(jobService).getJobById(1L);
    }

    // Test 3: Create job successfully
    @Test
    void createJob_withValidRequest_returnsCreated()
            throws Exception {

        JobResponseDTO response = new JobResponseDTO(
                101L,
                "Java Developer",
                "ABC Company",
                "Noida",
                7L,
                "recruiter1"
        );

        when(jobService.saveJob(any(JobRequestDTO.class)))
                .thenReturn(response);

        String requestJson = """
                {
                    "title": "Java Developer",
                    "company": "ABC Company",
                    "location": "Noida"
                }
                """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(101))
                .andExpect(jsonPath("$.title")
                        .value("Java Developer"))
                .andExpect(jsonPath("$.company")
                        .value("ABC Company"))
                .andExpect(jsonPath("$.location")
                        .value("Noida"));

        verify(jobService).saveJob(any(JobRequestDTO.class));
    }

    // Test 4: Update job successfully
    @Test
    void updateJob_whenJobExists_returnsOk()
            throws Exception {

        Job updatedJob = new Job();
        updatedJob.setId(1L);
        updatedJob.setTitle("Senior Java Developer");
        updatedJob.setCompany("XYZ Company");
        updatedJob.setLocation("Delhi");

        when(jobService.updateJob(eq(1L), any(Job.class)))
                .thenReturn(updatedJob);

        String requestJson = """
                {
                    "title": "Senior Java Developer",
                    "company": "XYZ Company",
                    "location": "Delhi"
                }
                """;

        mockMvc.perform(put("/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Senior Java Developer"))
                .andExpect(jsonPath("$.company")
                        .value("XYZ Company"))
                .andExpect(jsonPath("$.location")
                        .value("Delhi"));

        verify(jobService).updateJob(eq(1L), any(Job.class));
    }

    // Test 5: Update missing job
    @Test
    void updateJob_whenJobDoesNotExist_returnsNotFound()
            throws Exception {

        when(jobService.updateJob(eq(99L), any(Job.class)))
                .thenReturn(null);

        String requestJson = """
                {
                    "title": "Java Developer",
                    "company": "ABC Company",
                    "location": "Noida"
                }
                """;

        mockMvc.perform(put("/jobs/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());

        verify(jobService).updateJob(eq(99L), any(Job.class));
    }

    // Test 6: Delete job successfully
    @Test
    void deleteJob_whenJobExists_returnsNoContent()
            throws Exception {

        when(jobService.deleteJob(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(jobService).deleteJob(1L);
    }

    // Test 7: Delete missing job
    @Test
    void deleteJob_whenJobDoesNotExist_returnsNotFound()
            throws Exception {

        when(jobService.deleteJob(99L))
                .thenReturn(false);

        mockMvc.perform(delete("/jobs/99"))
                .andExpect(status().isNotFound());

        verify(jobService).deleteJob(99L);
    }

    // Test 8: Blank title returns validation error
    @Test
    void createJob_whenTitleIsBlank_returnsBadRequest()
            throws Exception {

        String requestJson = """
                {
                    "title": "",
                    "company": "ABC Company",
                    "location": "Noida"
                }
                """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title")
                        .value("Title is required"));

        verifyNoInteractions(jobService);
    }

    // Test 9: Missing company returns validation error
    @Test
    void createJob_whenCompanyIsMissing_returnsBadRequest()
            throws Exception {

        String requestJson = """
                {
                    "title": "Java Developer",
                    "location": "Noida"
                }
                """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.company")
                        .value("Company is required"));

        verifyNoInteractions(jobService);
    }

    // Test 10: Blank location returns validation error
    @Test
    void createJob_whenLocationIsBlank_returnsBadRequest()
            throws Exception {

        String requestJson = """
                {
                    "title": "Java Developer",
                    "company": "ABC Company",
                    "location": " "
                }
                """;

        mockMvc.perform(post("/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.location")
                        .value("Location is required"));

        verifyNoInteractions(jobService);
    }

    // Test 11: Job not found exception returns 404
    @Test
    void getJobById_whenServiceThrowsNotFound_returns404()
            throws Exception {

        when(jobService.getJobById(999L))
                .thenThrow(
                        new JobNotFoundException(
                                "Job not found with id: 999"
                        )
                );

        mockMvc.perform(get("/jobs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Job not found with id: 999"));

        verify(jobService).getJobById(999L);
    }
}
