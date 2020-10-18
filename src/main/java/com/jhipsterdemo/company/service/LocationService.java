package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.Location;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service Interface for managing {@link Location}.
 */
public interface LocationService {

    /**
     * Save a location.
     *
     * @param location the entity to save.
     * @return the persisted entity.
     */
    Mono<Location> save(Location location);

    /**
     * Get all the locations.
     *
     * @return the list of entities.
     */
    Flux<Location> findAll();

    /**
    * Returns the number of locations available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of locations available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" location.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Location> findOne(String id);

    /**
     * Delete the "id" location.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the location corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    Flux<Location> search(String query);
}
