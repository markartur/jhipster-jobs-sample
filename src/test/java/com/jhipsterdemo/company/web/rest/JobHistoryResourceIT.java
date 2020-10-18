package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.CompanyApp;
import com.jhipsterdemo.company.domain.JobHistory;
import com.jhipsterdemo.company.repository.JobHistoryRepository;
import com.jhipsterdemo.company.repository.search.JobHistorySearchRepository;
import com.jhipsterdemo.company.service.JobHistoryService;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import com.jhipsterdemo.company.domain.enumeration.Language;
/**
 * Integration tests for the {@link JobHistoryResource} REST controller.
 */
@SpringBootTest(classes = CompanyApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class JobHistoryResourceIT {

    private static final Instant DEFAULT_START_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_START_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_END_DATE = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_END_DATE = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Language DEFAULT_LANGUAGE = Language.FRENCH;
    private static final Language UPDATED_LANGUAGE = Language.ENGLISH;

    @Autowired
    private JobHistoryRepository jobHistoryRepository;

    @Autowired
    private JobHistoryService jobHistoryService;

    /**
     * This repository is mocked in the com.jhipsterdemo.company.repository.search test package.
     *
     * @see com.jhipsterdemo.company.repository.search.JobHistorySearchRepositoryMockConfiguration
     */
    @Autowired
    private JobHistorySearchRepository mockJobHistorySearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private JobHistory jobHistory;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JobHistory createEntity() {
        JobHistory jobHistory = new JobHistory()
            .startDate(DEFAULT_START_DATE)
            .endDate(DEFAULT_END_DATE)
            .language(DEFAULT_LANGUAGE);
        return jobHistory;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static JobHistory createUpdatedEntity() {
        JobHistory jobHistory = new JobHistory()
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .language(UPDATED_LANGUAGE);
        return jobHistory;
    }

    @BeforeEach
    public void initTest() {
        jobHistoryRepository.deleteAll().block();
        jobHistory = createEntity();
    }

    @Test
    public void createJobHistory() throws Exception {
        int databaseSizeBeforeCreate = jobHistoryRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockJobHistorySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the JobHistory
        webTestClient.post().uri("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobHistory))
            .exchange()
            .expectStatus().isCreated();

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll().collectList().block();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeCreate + 1);
        JobHistory testJobHistory = jobHistoryList.get(jobHistoryList.size() - 1);
        assertThat(testJobHistory.getStartDate()).isEqualTo(DEFAULT_START_DATE);
        assertThat(testJobHistory.getEndDate()).isEqualTo(DEFAULT_END_DATE);
        assertThat(testJobHistory.getLanguage()).isEqualTo(DEFAULT_LANGUAGE);

        // Validate the JobHistory in Elasticsearch
        verify(mockJobHistorySearchRepository, times(1)).save(testJobHistory);
    }

    @Test
    public void createJobHistoryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = jobHistoryRepository.findAll().collectList().block().size();

        // Create the JobHistory with an existing ID
        jobHistory.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobHistory))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll().collectList().block();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeCreate);

        // Validate the JobHistory in Elasticsearch
        verify(mockJobHistorySearchRepository, times(0)).save(jobHistory);
    }


    @Test
    public void getAllJobHistories() {
        // Initialize the database
        jobHistoryRepository.save(jobHistory).block();

        // Get all the jobHistoryList
        webTestClient.get().uri("/api/job-histories?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(jobHistory.getId()))
            .jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString()))
            .jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString()));
    }
    
    @Test
    public void getJobHistory() {
        // Initialize the database
        jobHistoryRepository.save(jobHistory).block();

        // Get the jobHistory
        webTestClient.get().uri("/api/job-histories/{id}", jobHistory.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(jobHistory.getId()))
            .jsonPath("$.startDate").value(is(DEFAULT_START_DATE.toString()))
            .jsonPath("$.endDate").value(is(DEFAULT_END_DATE.toString()))
            .jsonPath("$.language").value(is(DEFAULT_LANGUAGE.toString()));
    }
    @Test
    public void getNonExistingJobHistory() {
        // Get the jobHistory
        webTestClient.get().uri("/api/job-histories/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateJobHistory() throws Exception {
        // Configure the mock search repository
        when(mockJobHistorySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        jobHistoryService.save(jobHistory).block();

        int databaseSizeBeforeUpdate = jobHistoryRepository.findAll().collectList().block().size();

        // Update the jobHistory
        JobHistory updatedJobHistory = jobHistoryRepository.findById(jobHistory.getId()).block();
        updatedJobHistory
            .startDate(UPDATED_START_DATE)
            .endDate(UPDATED_END_DATE)
            .language(UPDATED_LANGUAGE);

        webTestClient.put().uri("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedJobHistory))
            .exchange()
            .expectStatus().isOk();

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll().collectList().block();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeUpdate);
        JobHistory testJobHistory = jobHistoryList.get(jobHistoryList.size() - 1);
        assertThat(testJobHistory.getStartDate()).isEqualTo(UPDATED_START_DATE);
        assertThat(testJobHistory.getEndDate()).isEqualTo(UPDATED_END_DATE);
        assertThat(testJobHistory.getLanguage()).isEqualTo(UPDATED_LANGUAGE);

        // Validate the JobHistory in Elasticsearch
        verify(mockJobHistorySearchRepository, times(2)).save(testJobHistory);
    }

    @Test
    public void updateNonExistingJobHistory() throws Exception {
        int databaseSizeBeforeUpdate = jobHistoryRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/job-histories")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(jobHistory))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the JobHistory in the database
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll().collectList().block();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeUpdate);

        // Validate the JobHistory in Elasticsearch
        verify(mockJobHistorySearchRepository, times(0)).save(jobHistory);
    }

    @Test
    public void deleteJobHistory() {
        // Configure the mock search repository
        when(mockJobHistorySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockJobHistorySearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        jobHistoryService.save(jobHistory).block();

        int databaseSizeBeforeDelete = jobHistoryRepository.findAll().collectList().block().size();

        // Delete the jobHistory
        webTestClient.delete().uri("/api/job-histories/{id}", jobHistory.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<JobHistory> jobHistoryList = jobHistoryRepository.findAll().collectList().block();
        assertThat(jobHistoryList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the JobHistory in Elasticsearch
        verify(mockJobHistorySearchRepository, times(1)).deleteById(jobHistory.getId());
    }

    @Test
    public void searchJobHistory() {
        // Configure the mock search repository
        when(mockJobHistorySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockJobHistorySearchRepository.count()).thenReturn(Mono.just(1L));
        // Initialize the database
        jobHistoryService.save(jobHistory).block();
        when(mockJobHistorySearchRepository.search("id:" + jobHistory.getId(), PageRequest.of(0, 20)))
            .thenReturn(Flux.just(jobHistory));

        // Search the jobHistory
        webTestClient.get().uri("/api/_search/job-histories?query=id:" + jobHistory.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(jobHistory.getId()))
            .jsonPath("$.[*].startDate").value(hasItem(DEFAULT_START_DATE.toString()))
            .jsonPath("$.[*].endDate").value(hasItem(DEFAULT_END_DATE.toString()))
            .jsonPath("$.[*].language").value(hasItem(DEFAULT_LANGUAGE.toString()));
    }
}
