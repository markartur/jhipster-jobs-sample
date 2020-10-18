package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service Interface for managing {@link Task}.
 */
public interface TaskService {

    /**
     * Save a task.
     *
     * @param task the entity to save.
     * @return the persisted entity.
     */
    Mono<Task> save(Task task);

    /**
     * Get all the tasks.
     *
     * @return the list of entities.
     */
    Flux<Task> findAll();

    /**
    * Returns the number of tasks available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of tasks available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" task.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Task> findOne(String id);

    /**
     * Delete the "id" task.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the task corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    Flux<Task> search(String query);
}
