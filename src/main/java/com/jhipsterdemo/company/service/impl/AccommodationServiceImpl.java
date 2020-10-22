package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.AccommodationService;
import com.jhipsterdemo.company.domain.Accommodation;
import com.jhipsterdemo.company.repository.AccommodationRepository;
import com.jhipsterdemo.company.repository.search.AccommodationSearchRepository;
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
 * Service Implementation for managing {@link Accommodation}.
 */
@Service
public class AccommodationServiceImpl implements AccommodationService {

    private final Logger log = LoggerFactory.getLogger(AccommodationServiceImpl.class);

    private final AccommodationRepository accommodationRepository;

    private final AccommodationSearchRepository accommodationSearchRepository;

    public AccommodationServiceImpl(AccommodationRepository accommodationRepository, AccommodationSearchRepository accommodationSearchRepository) {
        this.accommodationRepository = accommodationRepository;
        this.accommodationSearchRepository = accommodationSearchRepository;
    }

    @Override
    public Mono<Accommodation> save(Accommodation accommodation) {
        log.debug("Request to save Accommodation : {}", accommodation);
        return accommodationRepository.save(accommodation)
            .flatMap(accommodationSearchRepository::save)
;    }

    @Override
    public Flux<Accommodation> findAll() {
        log.debug("Request to get all Accommodations");
        return accommodationRepository.findAll();
    }


    public Mono<Long> countAll() {
        return accommodationRepository.count();
    }

    public Mono<Long> searchCount() {
        return accommodationSearchRepository.count();
    }

    @Override
    public Mono<Accommodation> findOne(String id) {
        log.debug("Request to get Accommodation : {}", id);
        return accommodationRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Accommodation : {}", id);
        return accommodationRepository.deleteById(id)
            .then(accommodationSearchRepository.deleteById(id));    }

    @Override
    public Flux<Accommodation> search(String query) {
        log.debug("Request to search Accommodations for query {}", query);
        return accommodationSearchRepository.search(query);
    }
}
