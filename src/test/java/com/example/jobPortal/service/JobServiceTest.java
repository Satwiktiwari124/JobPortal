
package com.example.jobPortal.service;

import com.example.jobPortal.dto.JobRequestDTO;
import com.example.jobPortal.dto.JobResponseDTO;
import com.example.jobPortal.entity.Job;
import com.example.jobPortal.entity.User;
import com.example.jobPortal.exception.JobNotFoundException;
import com.example.jobPortal.repository.JobRepository;
import com.example.jobPortal.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobService jobService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // Test 1: Get all jobs
    @Test
    void getAllJobs_returnsAllJobs() {

        List<Job> expectedJobs = List.of(
                new Job(),
                new Job()
        );

        when(jobRepository.findAllWithRecruiter())
                .thenReturn(expectedJobs);

        List<Job> actualJobs = jobService.getAllJobs();

        assertEquals(2, actualJobs.size());
        assertSame(expectedJobs, actualJobs);

        verify(jobRepository).findAllWithRecruiter();
    }

    // Test 2: Job exists
    @Test
    void getJobById_whenJobExists_returnsJob() {

        Job expectedJob = new Job();

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(expectedJob));

        Job actualJob = jobService.getJobById(1L);

        assertSame(expectedJob, actualJob);

        verify(jobRepository).findById(1L);
    }

    // Test 3: Job does not exist
    @Test
    void getJobById_whenJobDoesNotExist_throwsException() {

        when(jobRepository.findById(99L))
                .thenReturn(Optional.empty());

        JobNotFoundException exception = assertThrows(
                JobNotFoundException.class,
                () -> jobService.getJobById(99L)
        );

        assertEquals(
                "Job not found with id: 99",
                exception.getMessage()
        );

        verify(jobRepository).findById(99L);
    }

    // Test 4: Save job successfully
    @Test
    void saveJob_whenRecruiterAuthenticated_savesJob() {

        setAuthenticatedUser("recruiter1");

        User recruiter = createRecruiter(7L, "recruiter1");

        when(userRepository.findByUsername("recruiter1"))
                .thenReturn(Optional.of(recruiter));

        JobRequestDTO request = new JobRequestDTO();
        request.setTitle("Java Developer");
        request.setCompany("ABC Company");
        request.setLocation("Noida");

        when(jobRepository.save(any(Job.class)))
                .thenAnswer(invocation -> {
                    Job job = invocation.getArgument(0);
                    job.setId(101L);
                    return job;
                });

        JobResponseDTO response = jobService.saveJob(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("Java Developer", response.getTitle());
        assertEquals("ABC Company", response.getCompany());
        assertEquals("Noida", response.getLocation());

        verify(userRepository).findByUsername("recruiter1");

        ArgumentCaptor<Job> captor =
                ArgumentCaptor.forClass(Job.class);

        verify(jobRepository).save(captor.capture());

        Job savedJob = captor.getValue();

        assertEquals("Java Developer", savedJob.getTitle());
        assertEquals("ABC Company", savedJob.getCompany());
        assertEquals("Noida", savedJob.getLocation());
        assertSame(recruiter, savedJob.getRecruiter());
    }

    // Test 5: Update job successfully
    @Test
    void updateJob_whenOwnerAuthenticated_updatesJob() {

        setAuthenticatedUser("recruiter1");

        User recruiter = createRecruiter(7L, "recruiter1");

        Job existingJob = createJob(
                101L, "Old Title", "Old Company",
                "Delhi", recruiter
        );

        Job updatedData = new Job();
        updatedData.setTitle("Senior Java Developer");
        updatedData.setCompany("XYZ Company");
        updatedData.setLocation("Noida");

        when(jobRepository.findById(101L))
                .thenReturn(Optional.of(existingJob));

        when(jobRepository.save(any(Job.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Job result = jobService.updateJob(101L, updatedData);

        assertNotNull(result);
        assertEquals("Senior Java Developer", result.getTitle());
        assertEquals("XYZ Company", result.getCompany());
        assertEquals("Noida", result.getLocation());
        assertSame(recruiter, result.getRecruiter());

        verify(jobRepository).findById(101L);
        verify(jobRepository).save(existingJob);
    }

    // Test 6: Update missing job
    @Test
    void updateJob_whenJobDoesNotExist_returnsNull() {

        setAuthenticatedUser("recruiter1");

        when(jobRepository.findById(999L))
                .thenReturn(Optional.empty());

        Job updatedData = new Job();
        updatedData.setTitle("Java Developer");

        Job result = jobService.updateJob(999L, updatedData);

        assertNull(result);

        verify(jobRepository).findById(999L);
        verify(jobRepository, never()).save(any(Job.class));
    }

    // Test 7: Cannot update another recruiter's job
    @Test
    void updateJob_whenNotOwner_throwsAccessDeniedException() {

        setAuthenticatedUser("recruiter2");

        User owner = createRecruiter(7L, "recruiter1");

        Job existingJob = createJob(
                101L, "Java Developer", "ABC Company",
                "Delhi", owner
        );

        when(jobRepository.findById(101L))
                .thenReturn(Optional.of(existingJob));

        Job updatedData = new Job();
        updatedData.setTitle("Updated Title");

        assertThrows(
                AccessDeniedException.class,
                () -> jobService.updateJob(101L, updatedData)
        );

        verify(jobRepository).findById(101L);
        verify(jobRepository, never()).save(any(Job.class));
    }

    // Test 8: Delete job successfully
    @Test
    void deleteJob_whenOwnerAuthenticated_deletesJob() {

        setAuthenticatedUser("recruiter1");

        User recruiter = createRecruiter(7L, "recruiter1");

        Job existingJob = createJob(
                101L, "Java Developer", "ABC Company",
                "Noida", recruiter
        );

        when(jobRepository.findById(101L))
                .thenReturn(Optional.of(existingJob));

        boolean result = jobService.deleteJob(101L);

        assertTrue(result);

        verify(jobRepository).findById(101L);
        verify(jobRepository).delete(existingJob);
    }

    // Test 9: Delete missing job
    @Test
    void deleteJob_whenJobDoesNotExist_returnsFalse() {

        setAuthenticatedUser("recruiter1");

        when(jobRepository.findById(999L))
                .thenReturn(Optional.empty());

        boolean result = jobService.deleteJob(999L);

        assertFalse(result);

        verify(jobRepository).findById(999L);
        verify(jobRepository, never()).delete(any(Job.class));
    }

    // Test 10: Cannot delete another recruiter's job
    @Test
    void deleteJob_whenNotOwner_throwsAccessDeniedException() {

        setAuthenticatedUser("recruiter2");

        User owner = createRecruiter(7L, "recruiter1");

        Job existingJob = createJob(
                101L, "Java Developer", "ABC Company",
                "Delhi", owner
        );

        when(jobRepository.findById(101L))
                .thenReturn(Optional.of(existingJob));

        assertThrows(
                AccessDeniedException.class,
                () -> jobService.deleteJob(101L)
        );

        verify(jobRepository).findById(101L);
        verify(jobRepository, never()).delete(any(Job.class));
    }

    // Helper: Set authenticated username
    private void setAuthenticatedUser(String username) {

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of()
                )
        );
    }

    // Helper: Create recruiter
    private User createRecruiter(Long id, String username) {

        User recruiter = new User();
        recruiter.setId(id);
        recruiter.setUsername(username);

        return recruiter;
    }

    // Helper: Create job
    private Job createJob(
            Long id,
            String title,
            String company,
            String location,
            User recruiter
    ) {

        Job job = new Job();
        job.setId(id);
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setRecruiter(recruiter);

        return job;
    }
}
