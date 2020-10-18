package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.CompanyApp;
import com.jhipsterdemo.company.domain.Task;
import com.jhipsterdemo.company.repository.TaskRepository;
import com.jhipsterdemo.company.repository.search.TaskSearchRepository;
import com.jhipsterdemo.company.service.TaskService;

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
 * Integration tests for the {@link TaskResource} REST controller.
 */
@SpringBootTest(classes = CompanyApp.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
public class TaskResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    /**
     * This repository is mocked in the com.jhipsterdemo.company.repository.search test package.
     *
     * @see com.jhipsterdemo.company.repository.search.TaskSearchRepositoryMockConfiguration
     */
    @Autowired
    private TaskSearchRepository mockTaskSearchRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Task task;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createEntity() {
        Task task = new Task()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION);
        return task;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createUpdatedEntity() {
        Task task = new Task()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION);
        return task;
    }

    @BeforeEach
    public void initTest() {
        taskRepository.deleteAll().block();
        task = createEntity();
    }

    @Test
    public void createTask() throws Exception {
        int databaseSizeBeforeCreate = taskRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockTaskSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Task
        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(task))
            .exchange()
            .expectStatus().isCreated();

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll().collectList().block();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate + 1);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTask.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);

        // Validate the Task in Elasticsearch
        verify(mockTaskSearchRepository, times(1)).save(testTask);
    }

    @Test
    public void createTaskWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = taskRepository.findAll().collectList().block().size();

        // Create the Task with an existing ID
        task.setId("existing_id");

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient.post().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(task))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll().collectList().block();
        assertThat(taskList).hasSize(databaseSizeBeforeCreate);

        // Validate the Task in Elasticsearch
        verify(mockTaskSearchRepository, times(0)).save(task);
    }


    @Test
    public void getAllTasksAsStream() {
        // Initialize the database
        taskRepository.save(task).block();

        List<Task> taskList = webTestClient.get().uri("/api/tasks")
            .accept(MediaType.APPLICATION_STREAM_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_STREAM_JSON)
            .returnResult(Task.class)
            .getResponseBody()
            .filter(task::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(taskList).isNotNull();
        assertThat(taskList).hasSize(1);
        Task testTask = taskList.get(0);
        assertThat(testTask.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testTask.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    public void getAllTasks() {
        // Initialize the database
        taskRepository.save(task).block();

        // Get all the taskList
        webTestClient.get().uri("/api/tasks?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(task.getId()))
            .jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION));
    }
    
    @Test
    public void getTask() {
        // Initialize the database
        taskRepository.save(task).block();

        // Get the task
        webTestClient.get().uri("/api/tasks/{id}", task.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").value(is(task.getId()))
            .jsonPath("$.title").value(is(DEFAULT_TITLE))
            .jsonPath("$.description").value(is(DEFAULT_DESCRIPTION));
    }
    @Test
    public void getNonExistingTask() {
        // Get the task
        webTestClient.get().uri("/api/tasks/{id}", Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    public void updateTask() throws Exception {
        // Configure the mock search repository
        when(mockTaskSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        taskService.save(task).block();

        int databaseSizeBeforeUpdate = taskRepository.findAll().collectList().block().size();

        // Update the task
        Task updatedTask = taskRepository.findById(task.getId()).block();
        updatedTask
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION);

        webTestClient.put().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedTask))
            .exchange()
            .expectStatus().isOk();

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll().collectList().block();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);
        Task testTask = taskList.get(taskList.size() - 1);
        assertThat(testTask.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testTask.getDescription()).isEqualTo(UPDATED_DESCRIPTION);

        // Validate the Task in Elasticsearch
        verify(mockTaskSearchRepository, times(2)).save(testTask);
    }

    @Test
    public void updateNonExistingTask() throws Exception {
        int databaseSizeBeforeUpdate = taskRepository.findAll().collectList().block().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient.put().uri("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(task))
            .exchange()
            .expectStatus().isBadRequest();

        // Validate the Task in the database
        List<Task> taskList = taskRepository.findAll().collectList().block();
        assertThat(taskList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Task in Elasticsearch
        verify(mockTaskSearchRepository, times(0)).save(task);
    }

    @Test
    public void deleteTask() {
        // Configure the mock search repository
        when(mockTaskSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(mockTaskSearchRepository.deleteById(anyString())).thenReturn(Mono.empty());
        // Initialize the database
        taskService.save(task).block();

        int databaseSizeBeforeDelete = taskRepository.findAll().collectList().block().size();

        // Delete the task
        webTestClient.delete().uri("/api/tasks/{id}", task.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNoContent();

        // Validate the database contains one less item
        List<Task> taskList = taskRepository.findAll().collectList().block();
        assertThat(taskList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Task in Elasticsearch
        verify(mockTaskSearchRepository, times(1)).deleteById(task.getId());
    }

    @Test
    public void searchTask() {
        // Configure the mock search repository
        when(mockTaskSearchRepository.save(any()))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        taskService.save(task).block();
        when(mockTaskSearchRepository.search("id:" + task.getId()))
            .thenReturn(Flux.just(task));

        // Search the task
        webTestClient.get().uri("/api/_search/tasks?query=id:" + task.getId())
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id").value(hasItem(task.getId()))
            .jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE))
            .jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION));
    }
}
