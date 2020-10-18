package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.CompanyApp;
import com.jhipsterdemo.company.domain.Job;
import com.jhipsterdemo.company.repository.JobRepository;
import com.jhipsterdemo.company.repository.search.JobSearchRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the {@link JobResource} REST controller.
 */
@SpringBootTest(classes = CompanyApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class JobResourceIT {

    private static final String DEFAULT_JOB_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_JOB_TITLE = "BBBBBBBBBB";

    private static final Long DEFAULT_MIN_SALARY = 1L;
    private static final Long UPDATED_MIN_SALARY = 2L;

    private static final Long DEFAULT_MAX_SALARY = 1L;
    private static final Long UPDATED_MAX_SALARY = 2L;

    @Autowired
    private JobRepository jobRepository;

    @Mock
    private JobRepository jobRepositoryMock;

    /**
     * This repository is mocked in the com.jhipsterdemo.company.repository.search test package.
     *
     * @see com.jhipsterdemo.company.repository.search.JobSearchRepositoryMockConfiguration
     */
    @Autowired
    private JobSearchRepository mockJobSearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Job job;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Job createEntity() {
        Job job = new Job()
            .jobTitle(DEFAULT_JOB_TITLE)
            .minSalary(DEFAULT_MIN_SALARY)
            .maxSalary(DEFAULT_MAX_SALARY);
        return job;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Job createUpdatedEntity() {
        Job job = new Job()
            .jobTitle(UPDATED_JOB_TITLE)
            .minSalary(UPDATED_MIN_SALARY)
            .maxSalary(UPDATED_MAX_SALARY);
        return job;
    }

    @BeforeEach
    public void initTest() {
        jobRepository.deleteAll().block();
        job = createEntity();
    }

    @Test
    public void createJob() throws Exception {
        int databaseSizeBeforeCreate = jobRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockJobSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Job
        webTestClient.post().uri("/api/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(job))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeCreate + 1);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(DEFAULT_JOB_TITLE);
        assertThat(testJob.getMinSalary()).isEqualTo(DEFAULT_MIN_SALARY);
        assertThat(testJob.getMaxSalary()).isEqualTo(DEFAULT_MAX_SALARY);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(1)).save(testJob);
    }

    @Test
    public void createJobWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = jobRepository.findAll().collectList().block().size();

        // Create the Job with an existing ID
        job.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(job))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeCreate);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(0)).save(job);
    }


    @Test
    public void getAllJobs() {
        // Initialize the database
        jobRepository.save(job).block();

        // Get all the jobList
        webTestClient.get().uri("/api/jobs?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(job.getId()))
            .jsonPath("$.[*].jobTitle").value(hasItem(DEFAULT_JOB_TITLE))
            .jsonPath("$.[*].minSalary").value(hasItem(DEFAULT_MIN_SALARY.intValue()))
            .jsonPath("$.[*].maxSalary").value(hasItem(DEFAULT_MAX_SALARY.intValue()));
    }
    
    @SuppressWarnings({"unchecked"})
    public void getAllJobsWithEagerRelationshipsIsEnabled() {
        when(jobRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/jobs?eagerload=true")
            .exchange()
            .expectStatus().isOk();

        verify(jobRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({"unchecked"})
    public void getAllJobsWithEagerRelationshipsIsNotEnabled() {
        when(jobRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/jobs?eagerload=true")
            .exchange()
            .expectStatus().isOk();

        verify(jobRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    public void getJob() {
        // Initialize the database
        jobRepository.save(job).block();

        // Get the job
        webTestClient.get().uri("/api/jobs/{id}", job.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(job.getId()))
            .jsonPath("$.jobTitle").value(is(DEFAULT_JOB_TITLE))
            .jsonPath("$.minSalary").value(is(DEFAULT_MIN_SALARY.intValue()))
            .jsonPath("$.maxSalary").value(is(DEFAULT_MAX_SALARY.intValue()));
    }
    @Test
    public void getNonExistingJob() {
        // Get the job
        webTestClient.get().uri("/api/jobs/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateJob() throws Exception {
        // Configure the mock search repository
        when(mockJobSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        jobRepository.save(job).block();

        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();

        // Update the job
        Job updatedJob = jobRepository.findById(job.getId()).block();
        updatedJob
            .jobTitle(UPDATED_JOB_TITLE)
            .minSalary(UPDATED_MIN_SALARY)
            .maxSalary(UPDATED_MAX_SALARY);

        webTestClient.put().uri("/api/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedJob))
            .exchange()
            .expectStatus().isOk();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);
        Job testJob = jobList.get(jobList.size() - 1);
        assertThat(testJob.getJobTitle()).isEqualTo(UPDATED_JOB_TITLE);
        assertThat(testJob.getMinSalary()).isEqualTo(UPDATED_MIN_SALARY);
        assertThat(testJob.getMaxSalary()).isEqualTo(UPDATED_MAX_SALARY);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(1)).save(testJob);
    }

    @Test
    public void updateNonExistingJob() throws Exception {
        int databaseSizeBeforeUpdate = jobRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/jobs")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(job))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Job in the database
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(0)).save(job);
    }

    @Test
    public void deleteJob() {
        // Configure the mock search repository
        when(mockJobSearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        jobRepository.save(job).block();

        int databaseSizeBeforeDelete = jobRepository.findAll().collectList().block().size();

        // Delete the job
        webTestClient.delete().uri("/api/jobs/{id}", job.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Job> jobList = jobRepository.findAll().collectList().block();
        assertThat(jobList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Job in Elasticsearch
        verify(mockJobSearchRepository, times(1)).deleteById(job.getId());
    }

    @Test
    public void searchJob() {
        // Configure the mock search repository
        when(mockJobSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockJobSearchRepository.count()).thenReturn(Mono.just(1L));
        // Initialize the database
        jobRepository.save(job).block();
        when(mockJobSearchRepository.search("id:" + job.getId(), PageRequest.of(0, 20)))
            .thenReturn(Flux.just(job));

        // Search the job
        webTestClient.get().uri("/api/_search/jobs?query=id:" + job.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(job.getId()))
            .jsonPath("$.[*].jobTitle").value(hasItem(DEFAULT_JOB_TITLE))
            .jsonPath("$.[*].minSalary").value(hasItem(DEFAULT_MIN_SALARY.intValue()))
            .jsonPath("$.[*].maxSalary").value(hasItem(DEFAULT_MAX_SALARY.intValue()));
    }
}
