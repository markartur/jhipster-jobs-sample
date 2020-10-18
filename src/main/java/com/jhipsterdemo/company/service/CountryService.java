package com.jhipsterdemo.company.service;

import com.jhipsterdemo.company.domain.Country;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service Interface for managing {@link Country}.
 */
public interface CountryService {

    /**
     * Save a country.
     *
     * @param country the entity to save.
     * @return the persisted entity.
     */
    Mono<Country> save(Country country);

    /**
     * Get all the countries.
     *
     * @return the list of entities.
     */
    Flux<Country> findAll();

    /**
    * Returns the number of countries available.
    *
    */
    Mono<Long> countAll();

    /**
    * Returns the number of countries available in search repository.
    *
    */
    Mono<Long> searchCount();


    /**
     * Get the "id" country.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Mono<Country> findOne(String id);

    /**
     * Delete the "id" country.
     *
     * @param id the id of the entity.
     */
    Mono<Void> delete(String id);

    /**
     * Search for the country corresponding to the query.
     *
     * @param query the query of the search.
     * 
     * @return the list of entities.
     */
    Flux<Country> search(String query);
}
