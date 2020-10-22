package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.Accommodation;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service Interface for managing {@link Accommodation}.
 */
public interface AccommodationService {

    /**
     * Save a accommodation.
     *
     * @param accommodation the entity to save.
     * @return the persisted entity.
     */
    Mono<Accommodation> save(Accommodation accommodation);

    /**
     * Get all the accommodations.
     *
     * @return the list of entities.
     */
    Flux<Accommodation> findAll();

    /**
    * Returns the number of accommodations available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of accommodations available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" accommodation.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Accommodation> findOne(String id);

    /**
     * Delete the "id" accommodation.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the accommodation corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    Flux<Accommodation> search(String query);
}
