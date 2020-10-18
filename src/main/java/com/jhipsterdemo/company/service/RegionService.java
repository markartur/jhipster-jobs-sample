package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.Region;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service Interface for managing {@link Region}.
 */
public interface RegionService {

    /**
     * Save a region.
     *
     * @param region the entity to save.
     * @return the persisted entity.
     */
    Mono<Region> save(Region region);

    /**
     * Get all the regions.
     *
     * @return the list of entities.
     */
    Flux<Region> findAll();

    /**
    * Returns the number of regions available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of regions available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" region.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Region> findOne(String id);

    /**
     * Delete the "id" region.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the region corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    Flux<Region> search(String query);
}
