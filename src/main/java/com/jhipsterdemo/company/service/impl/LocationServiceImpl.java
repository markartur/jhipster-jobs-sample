package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.LocationService;
import com.jhipsterdemo.company.domain.Location;
import com.jhipsterdemo.company.repository.LocationRepository;
import com.jhipsterdemo.company.repository.search.LocationSearchRepository;
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
 * Service Implementation for managing {@link Location}.
 */
@Service
public class LocationServiceImpl implements LocationService {

    private final Logger log = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationRepository locationRepository;

    private final LocationSearchRepository locationSearchRepository;

    public LocationServiceImpl(LocationRepository locationRepository, LocationSearchRepository locationSearchRepository) {
        this.locationRepository = locationRepository;
        this.locationSearchRepository = locationSearchRepository;
    }

    @Override
    public Mono<Location> save(Location location) {
        log.debug("Request to save Location : {}", location);
        return locationRepository.save(location)
            .flatMap(locationSearchRepository::save)
;    }

    @Override
    public Flux<Location> findAll() {
        log.debug("Request to get all Locations");
        return locationRepository.findAll();
    }


    public Mono<Long> countAll() {
        return locationRepository.count();
    }

    public Mono<Long> searchCount() {
        return locationSearchRepository.count();
    }

    @Override
    public Mono<Location> findOne(String id) {
        log.debug("Request to get Location : {}", id);
        return locationRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Location : {}", id);
        return locationRepository.deleteById(id)
            .then(locationSearchRepository.deleteById(id));    }

    @Override
    public Flux<Location> search(String query) {
        log.debug("Request to search Locations for query {}", query);
        return locationSearchRepository.search(query);
    }
}
