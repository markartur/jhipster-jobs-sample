package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.JobHistory;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Service Interface for managing {@link JobHistory}.
 */
public interface JobHistoryService {

    /**
     * Save a jobHistory.
     *
     * @param jobHistory the entity to save.
     * @return the persisted entity.
     */
    Mono<JobHistory> save(JobHistory jobHistory);

    /**
     * Get all the jobHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<JobHistory> findAll(Pageable pageable);

    /**
    * Returns the number of jobHistories available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of jobHistories available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" jobHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<JobHistory> findOne(String id);

    /**
     * Delete the "id" jobHistory.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the jobHistory corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Flux<JobHistory> search(String query, Pageable pageable);
}
