package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.CountryService;
import com.jhipsterdemo.company.domain.Country;
import com.jhipsterdemo.company.repository.CountryRepository;
import com.jhipsterdemo.company.repository.search.CountrySearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing {@link Country}.
 */
@Service
public class CountryServiceImpl implements CountryService {

    private final Logger log = LoggerFactory.getLogger(CountryServiceImpl.class);

    private final CountryRepository countryRepository;

    private final CountrySearchRepository countrySearchRepository;

    public CountryServiceImpl(CountryRepository countryRepository, CountrySearchRepository countrySearchRepository) {
        this.countryRepository = countryRepository;
        this.countrySearchRepository = countrySearchRepository;
    }

    @Override
    public Mono<Country> save(Country country) {
        log.debug("Request to save Country : {}", country);
        return countryRepository.save(country)
            .flatMap(countrySearchRepository::save)
;    }

    @Override
    public Flux<Country> findAll() {
        log.debug("Request to get all Countries");
        return countryRepository.findAll();
    }


    public Mono<Long> countAll() {
        return countryRepository.count();
    }

    public Mono<Long> searchCount() {
        return countrySearchRepository.count();
    }

    @Override
    public Mono<Country> findOne(String id) {
        log.debug("Request to get Country : {}", id);
        return countryRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Country : {}", id);
        return countryRepository.deleteById(id)
            .then(countrySearchRepository.deleteById(id));    }

    @Override
    public Flux<Country> search(String query) {
        log.debug("Request to search Countries for query {}", query);
        return countrySearchRepository.search(query);
    }
}
