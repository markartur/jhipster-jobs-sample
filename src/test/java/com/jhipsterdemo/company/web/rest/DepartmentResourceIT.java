package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.CompanyApp;
import com.jhipsterdemo.company.domain.Department;
import com.jhipsterdemo.company.repository.DepartmentRepository;
import com.jhipsterdemo.company.repository.search.DepartmentSearchRepository;
import com.jhipsterdemo.company.service.DepartmentService;

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
 * Integration tests for the {@link DepartmentResource} REST controller.
 */
@SpringBootTest(classes = CompanyApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class DepartmentResourceIT {

    private static final String DEFAULT_DEPARTMENT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_DEPARTMENT_NAME = "BBBBBBBBBB";

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DepartmentService departmentService;

    /**
     * This repository is mocked in the com.jhipsterdemo.company.repository.search test package.
     *
     * @see com.jhipsterdemo.company.repository.search.DepartmentSearchRepositoryMockConfiguration
     */
    @Autowired
    private DepartmentSearchRepository mockDepartmentSearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Department department;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Department createEntity() {
        Department department = new Department()
            .departmentName(DEFAULT_DEPARTMENT_NAME);
        return department;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Department createUpdatedEntity() {
        Department department = new Department()
            .departmentName(UPDATED_DEPARTMENT_NAME);
        return department;
    }

    @BeforeEach
    public void initTest() {
        departmentRepository.deleteAll().block();
        department = createEntity();
    }

    @Test
    public void createDepartment() throws Exception {
        int databaseSizeBeforeCreate = departmentRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockDepartmentSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Department
        webTestClient.post().uri("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(department))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll().collectList().block();
        assertThat(departmentList).hasSize(databaseSizeBeforeCreate + 1);
        Department testDepartment = departmentList.get(departmentList.size() - 1);
        assertThat(testDepartment.getDepartmentName()).isEqualTo(DEFAULT_DEPARTMENT_NAME);

        // Validate the Department in Elasticsearch
        verify(mockDepartmentSearchRepository, times(1)).save(testDepartment);
    }

    @Test
    public void createDepartmentWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = departmentRepository.findAll().collectList().block().size();

        // Create the Department with an existing ID
        department.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(department))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll().collectList().block();
        assertThat(departmentList).hasSize(databaseSizeBeforeCreate);

        // Validate the Department in Elasticsearch
        verify(mockDepartmentSearchRepository, times(0)).save(department);
    }


    @Test
    public void checkDepartmentNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = departmentRepository.findAll().collectList().block().size();
        // set the field null
        department.setDepartmentName(null);

        // Create the Department, which fails.


        webTestClient.post().uri("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(department))
            .exchange()
            .expectStatus().isBadRequest();

        List<Department> departmentList = departmentRepository.findAll().collectList().block();
        assertThat(departmentList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    public void getAllDepartmentsAsStream() {
        // Initialize the database
        departmentRepository.save(department).block();

        List<Department> departmentList = webTestClient.get().uri("/api/departments")
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_STREAM_JSON)
            .returnResult(Department.class)
            .getResponseBody()
            .filter(department::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(departmentList).isNotNull();
        assertThat(departmentList).hasSize(1);
        Department testDepartment = departmentList.get(0);
        assertThat(testDepartment.getDepartmentName()).isEqualTo(DEFAULT_DEPARTMENT_NAME);
    }

    @Test
    public void getAllDepartments() {
        // Initialize the database
        departmentRepository.save(department).block();

        // Get all the departmentList
        webTestClient.get().uri("/api/departments?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(department.getId()))
            .jsonPath("$.[*].departmentName").value(hasItem(DEFAULT_DEPARTMENT_NAME));
    }
    
    @Test
    public void getDepartment() {
        // Initialize the database
        departmentRepository.save(department).block();

        // Get the department
        webTestClient.get().uri("/api/departments/{id}", department.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(department.getId()))
            .jsonPath("$.departmentName").value(is(DEFAULT_DEPARTMENT_NAME));
    }
    @Test
    public void getNonExistingDepartment() {
        // Get the department
        webTestClient.get().uri("/api/departments/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateDepartment() throws Exception {
        // Configure the mock search repository
        when(mockDepartmentSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        departmentService.save(department).block();

        int databaseSizeBeforeUpdate = departmentRepository.findAll().collectList().block().size();

        // Update the department
        Department updatedDepartment = departmentRepository.findById(department.getId()).block();
        updatedDepartment
            .departmentName(UPDATED_DEPARTMENT_NAME);

        webTestClient.put().uri("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedDepartment))
            .exchange()
            .expectStatus().isOk();

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll().collectList().block();
        assertThat(departmentList).hasSize(databaseSizeBeforeUpdate);
        Department testDepartment = departmentList.get(departmentList.size() - 1);
        assertThat(testDepartment.getDepartmentName()).isEqualTo(UPDATED_DEPARTMENT_NAME);

        // Validate the Department in Elasticsearch
        verify(mockDepartmentSearchRepository, times(2)).save(testDepartment);
    }

    @Test
    public void updateNonExistingDepartment() throws Exception {
        int databaseSizeBeforeUpdate = departmentRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/departments")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(department))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Department in the database
        List<Department> departmentList = departmentRepository.findAll().collectList().block();
        assertThat(departmentList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Department in Elasticsearch
        verify(mockDepartmentSearchRepository, times(0)).save(department);
    }

    @Test
    public void deleteDepartment() {
        // Configure the mock search repository
        when(mockDepartmentSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockDepartmentSearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        departmentService.save(department).block();

        int databaseSizeBeforeDelete = departmentRepository.findAll().collectList().block().size();

        // Delete the department
        webTestClient.delete().uri("/api/departments/{id}", department.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Department> departmentList = departmentRepository.findAll().collectList().block();
        assertThat(departmentList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Department in Elasticsearch
        verify(mockDepartmentSearchRepository, times(1)).deleteById(department.getId());
    }

    @Test
    public void searchDepartment() {
        // Configure the mock search repository
        when(mockDepartmentSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        departmentService.save(department).block();
        when(mockDepartmentSearchRepository.search("id:" + department.getId()))
            .thenReturn(Flux.just(department));

        // Search the department
        webTestClient.get().uri("/api/_search/departments?query=id:" + department.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(department.getId()))
            .jsonPath("$.[*].departmentName").value(hasItem(DEFAULT_DEPARTMENT_NAME));
    }
}
