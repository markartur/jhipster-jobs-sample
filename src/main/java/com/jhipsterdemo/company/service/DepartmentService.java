package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.Department;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service Interface for managing {@link Department}.
 */
public interface DepartmentService {

    /**
     * Save a department.
     *
     * @param department the entity to save.
     * @return the persisted entity.
     */
    Mono<Department> save(Department department);

    /**
     * Get all the departments.
     *
     * @return the list of entities.
     */
    Flux<Department> findAll();

    /**
    * Returns the number of departments available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of departments available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" department.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Department> findOne(String id);

    /**
     * Delete the "id" department.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the department corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    Flux<Department> search(String query);
}
