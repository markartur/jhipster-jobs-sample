package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.CompanyApp;
import com.jhipsterdemo.company.domain.Country;
import com.jhipsterdemo.company.repository.CountryRepository;
import com.jhipsterdemo.company.repository.search.CountrySearchRepository;
import com.jhipsterdemo.company.service.CountryService;

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
 * Integration tests for the {@link CountryResource} REST controller.
 */
@SpringBootTest(classes = CompanyApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class CountryResourceIT {

    private static final String DEFAULT_COUNTRY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COUNTRY_NAME = "BBBBBBBBBB";

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CountryService countryService;

    /**
     * This repository is mocked in the com.jhipsterdemo.company.repository.search test package.
     *
     * @see com.jhipsterdemo.company.repository.search.CountrySearchRepositoryMockConfiguration
     */
    @Autowired
    private CountrySearchRepository mockCountrySearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Country country;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Country createEntity() {
        Country country = new Country()
            .countryName(DEFAULT_COUNTRY_NAME);
        return country;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Country createUpdatedEntity() {
        Country country = new Country()
            .countryName(UPDATED_COUNTRY_NAME);
        return country;
    }

    @BeforeEach
    public void initTest() {
        countryRepository.deleteAll().block();
        country = createEntity();
    }

    @Test
    public void createCountry() throws Exception {
        int databaseSizeBeforeCreate = countryRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockCountrySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Country
        webTestClient.post().uri("/api/countries")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(country))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll().collectList().block();
        assertThat(countryList).hasSize(databaseSizeBeforeCreate + 1);
        Country testCountry = countryList.get(countryList.size() - 1);
        assertThat(testCountry.getCountryName()).isEqualTo(DEFAULT_COUNTRY_NAME);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(1)).save(testCountry);
    }

    @Test
    public void createCountryWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = countryRepository.findAll().collectList().block().size();

        // Create the Country with an existing ID
        country.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/countries")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(country))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll().collectList().block();
        assertThat(countryList).hasSize(databaseSizeBeforeCreate);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(0)).save(country);
    }


    @Test
    public void getAllCountriesAsStream() {
        // Initialize the database
        countryRepository.save(country).block();

        List<Country> countryList = webTestClient.get().uri("/api/countries")
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_STREAM_JSON)
            .returnResult(Country.class)
            .getResponseBody()
            .filter(country::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(countryList).isNotNull();
        assertThat(countryList).hasSize(1);
        Country testCountry = countryList.get(0);
        assertThat(testCountry.getCountryName()).isEqualTo(DEFAULT_COUNTRY_NAME);
    }

    @Test
    public void getAllCountries() {
        // Initialize the database
        countryRepository.save(country).block();

        // Get all the countryList
        webTestClient.get().uri("/api/countries?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(country.getId()))
            .jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME));
    }
    
    @Test
    public void getCountry() {
        // Initialize the database
        countryRepository.save(country).block();

        // Get the country
        webTestClient.get().uri("/api/countries/{id}", country.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(country.getId()))
            .jsonPath("$.countryName").value(is(DEFAULT_COUNTRY_NAME));
    }
    @Test
    public void getNonExistingCountry() {
        // Get the country
        webTestClient.get().uri("/api/countries/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateCountry() throws Exception {
        // Configure the mock search repository
        when(mockCountrySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        countryService.save(country).block();

        int databaseSizeBeforeUpdate = countryRepository.findAll().collectList().block().size();

        // Update the country
        Country updatedCountry = countryRepository.findById(country.getId()).block();
        updatedCountry
            .countryName(UPDATED_COUNTRY_NAME);

        webTestClient.put().uri("/api/countries")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedCountry))
            .exchange()
            .expectStatus().isOk();

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll().collectList().block();
        assertThat(countryList).hasSize(databaseSizeBeforeUpdate);
        Country testCountry = countryList.get(countryList.size() - 1);
        assertThat(testCountry.getCountryName()).isEqualTo(UPDATED_COUNTRY_NAME);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(2)).save(testCountry);
    }

    @Test
    public void updateNonExistingCountry() throws Exception {
        int databaseSizeBeforeUpdate = countryRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/countries")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(country))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Country in the database
        List<Country> countryList = countryRepository.findAll().collectList().block();
        assertThat(countryList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(0)).save(country);
    }

    @Test
    public void deleteCountry() {
        // Configure the mock search repository
        when(mockCountrySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockCountrySearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        countryService.save(country).block();

        int databaseSizeBeforeDelete = countryRepository.findAll().collectList().block().size();

        // Delete the country
        webTestClient.delete().uri("/api/countries/{id}", country.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Country> countryList = countryRepository.findAll().collectList().block();
        assertThat(countryList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Country in Elasticsearch
        verify(mockCountrySearchRepository, times(1)).deleteById(country.getId());
    }

    @Test
    public void searchCountry() {
        // Configure the mock search repository
        when(mockCountrySearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        countryService.save(country).block();
        when(mockCountrySearchRepository.search("id:" + country.getId()))
            .thenReturn(Flux.just(country));

        // Search the country
        webTestClient.get().uri("/api/_search/countries?query=id:" + country.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(country.getId()))
            .jsonPath("$.[*].countryName").value(hasItem(DEFAULT_COUNTRY_NAME));
    }
}
