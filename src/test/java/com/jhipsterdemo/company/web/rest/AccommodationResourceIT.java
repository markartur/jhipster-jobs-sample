package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.CompanyApp;
import com.jhipsterdemo.company.domain.Accommodation;
import com.jhipsterdemo.company.repository.AccommodationRepository;
import com.jhipsterdemo.company.repository.search.AccommodationSearchRepository;
import com.jhipsterdemo.company.service.AccommodationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the {@link AccommodationResource} REST controller.
 */
@SpringBootTest(classes = CompanyApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class AccommodationResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_HOTELIER = "AAAAAAAAAA";
    private static final String UPDATED_HOTELIER = "BBBBBBBBBB";

    private static final String DEFAULT_CATEGORY = "AAAAAAAAAA";
    private static final String UPDATED_CATEGORY = "BBBBBBBBBB";

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private AccommodationService accommodationService;

    /**
     * This repository is mocked in the com.jhipsterdemo.company.repository.search test package.
     *
     * @see com.jhipsterdemo.company.repository.search.AccommodationSearchRepositoryMockConfiguration
     */
    @Autowired
    private AccommodationSearchRepository mockAccommodationSearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Accommodation accommodation;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Accommodation createEntity() {
        Accommodation accommodation = new Accommodation()
            .name(DEFAULT_NAME)
            .hotelier(DEFAULT_HOTELIER)
            .category(DEFAULT_CATEGORY);
        return accommodation;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Accommodation createUpdatedEntity() {
        Accommodation accommodation = new Accommodation()
            .name(UPDATED_NAME)
            .hotelier(UPDATED_HOTELIER)
            .category(UPDATED_CATEGORY);
        return accommodation;
    }

    @BeforeEach
    public void initTest() {
        accommodationRepository.deleteAll().block();
        accommodation = createEntity();
    }

    @Test
    public void createAccommodation() throws Exception {
        int databaseSizeBeforeCreate = accommodationRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockAccommodationSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Accommodation
        webTestClient.post().uri("/api/accommodations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accommodation))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Accommodation in the database
        List<Accommodation> accommodationList = accommodationRepository.findAll().collectList().block();
        assertThat(accommodationList).hasSize(databaseSizeBeforeCreate + 1);
        Accommodation testAccommodation = accommodationList.get(accommodationList.size() - 1);
        assertThat(testAccommodation.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAccommodation.getHotelier()).isEqualTo(DEFAULT_HOTELIER);
        assertThat(testAccommodation.getCategory()).isEqualTo(DEFAULT_CATEGORY);

        // Validate the Accommodation in Elasticsearch
        verify(mockAccommodationSearchRepository, times(1)).save(testAccommodation);
    }

    @Test
    public void createAccommodationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = accommodationRepository.findAll().collectList().block().size();

        // Create the Accommodation with an existing ID
        accommodation.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/accommodations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accommodation))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Accommodation in the database
        List<Accommodation> accommodationList = accommodationRepository.findAll().collectList().block();
        assertThat(accommodationList).hasSize(databaseSizeBeforeCreate);

        // Validate the Accommodation in Elasticsearch
        verify(mockAccommodationSearchRepository, times(0)).save(accommodation);
    }


    @Test
    public void getAllAccommodationsAsStream() {
        // Initialize the database
        accommodationRepository.save(accommodation).block();

        List<Accommodation> accommodationList = webTestClient.get().uri("/api/accommodations")
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_STREAM_JSON)
            .returnResult(Accommodation.class)
            .getResponseBody()
            .filter(accommodation::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(accommodationList).isNotNull();
        assertThat(accommodationList).hasSize(1);
        Accommodation testAccommodation = accommodationList.get(0);
        assertThat(testAccommodation.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testAccommodation.getHotelier()).isEqualTo(DEFAULT_HOTELIER);
        assertThat(testAccommodation.getCategory()).isEqualTo(DEFAULT_CATEGORY);
    }

    @Test
    public void getAllAccommodations() {
        // Initialize the database
        accommodationRepository.save(accommodation).block();

        // Get all the accommodationList
        webTestClient.get().uri("/api/accommodations?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(accommodation.getId()))
            .jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].hotelier").value(hasItem(DEFAULT_HOTELIER))
            .jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY));
    }
    
    @Test
    public void getAccommodation() {
        // Initialize the database
        accommodationRepository.save(accommodation).block();

        // Get the accommodation
        webTestClient.get().uri("/api/accommodations/{id}", accommodation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(accommodation.getId()))
            .jsonPath("$.name").value(is(DEFAULT_NAME))
            .jsonPath("$.hotelier").value(is(DEFAULT_HOTELIER))
            .jsonPath("$.category").value(is(DEFAULT_CATEGORY));
    }
    @Test
    public void getNonExistingAccommodation() {
        // Get the accommodation
        webTestClient.get().uri("/api/accommodations/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateAccommodation() throws Exception {
        // Configure the mock search repository
        when(mockAccommodationSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        accommodationService.save(accommodation).block();

        int databaseSizeBeforeUpdate = accommodationRepository.findAll().collectList().block().size();

        // Update the accommodation
        Accommodation updatedAccommodation = accommodationRepository.findById(accommodation.getId()).block();
        updatedAccommodation
            .name(UPDATED_NAME)
            .hotelier(UPDATED_HOTELIER)
            .category(UPDATED_CATEGORY);

        webTestClient.put().uri("/api/accommodations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedAccommodation))
            .exchange()
            .expectStatus().isOk();

        // Validate the Accommodation in the database
        List<Accommodation> accommodationList = accommodationRepository.findAll().collectList().block();
        assertThat(accommodationList).hasSize(databaseSizeBeforeUpdate);
        Accommodation testAccommodation = accommodationList.get(accommodationList.size() - 1);
        assertThat(testAccommodation.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testAccommodation.getHotelier()).isEqualTo(UPDATED_HOTELIER);
        assertThat(testAccommodation.getCategory()).isEqualTo(UPDATED_CATEGORY);

        // Validate the Accommodation in Elasticsearch
        verify(mockAccommodationSearchRepository, times(2)).save(testAccommodation);
    }

    @Test
    public void updateNonExistingAccommodation() throws Exception {
        int databaseSizeBeforeUpdate = accommodationRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/accommodations")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(accommodation))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Accommodation in the database
        List<Accommodation> accommodationList = accommodationRepository.findAll().collectList().block();
        assertThat(accommodationList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Accommodation in Elasticsearch
        verify(mockAccommodationSearchRepository, times(0)).save(accommodation);
    }

    @Test
    public void deleteAccommodation() {
        // Configure the mock search repository
        when(mockAccommodationSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockAccommodationSearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        accommodationService.save(accommodation).block();

        int databaseSizeBeforeDelete = accommodationRepository.findAll().collectList().block().size();

        // Delete the accommodation
        webTestClient.delete().uri("/api/accommodations/{id}", accommodation.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Accommodation> accommodationList = accommodationRepository.findAll().collectList().block();
        assertThat(accommodationList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Accommodation in Elasticsearch
        verify(mockAccommodationSearchRepository, times(1)).deleteById(accommodation.getId());
    }

    @Test
    public void searchAccommodation() {
        // Configure the mock search repository
        when(mockAccommodationSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        accommodationService.save(accommodation).block();
        when(mockAccommodationSearchRepository.search("id:" + accommodation.getId()))
            .thenReturn(Flux.just(accommodation));

        // Search the accommodation
        webTestClient.get().uri("/api/_search/accommodations?query=id:" + accommodation.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(accommodation.getId()))
            .jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].hotelier").value(hasItem(DEFAULT_HOTELIER))
            .jsonPath("$.[*].category").value(hasItem(DEFAULT_CATEGORY));
    }
}
