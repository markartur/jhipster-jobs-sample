package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.RegionService;
import com.jhipsterdemo.company.domain.Region;
import com.jhipsterdemo.company.repository.RegionRepository;
import com.jhipsterdemo.company.repository.search.RegionSearchRepository;
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
 * Service Implementation for managing {@link Region}.
 */
@Service
public class RegionServiceImpl implements RegionService {

    private final Logger log = LoggerFactory.getLogger(RegionServiceImpl.class);

    private final RegionRepository regionRepository;

    private final RegionSearchRepository regionSearchRepository;

    public RegionServiceImpl(RegionRepository regionRepository, RegionSearchRepository regionSearchRepository) {
        this.regionRepository = regionRepository;
        this.regionSearchRepository = regionSearchRepository;
    }

    @Override
    public Mono<Region> save(Region region) {
        log.debug("Request to save Region : {}", region);
        return regionRepository.save(region)
            .flatMap(regionSearchRepository::save)
;    }

    @Override
    public Flux<Region> findAll() {
        log.debug("Request to get all Regions");
        return regionRepository.findAll();
    }


    public Mono<Long> countAll() {
        return regionRepository.count();
    }

    public Mono<Long> searchCount() {
        return regionSearchRepository.count();
    }

    @Override
    public Mono<Region> findOne(String id) {
        log.debug("Request to get Region : {}", id);
        return regionRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Region : {}", id);
        return regionRepository.deleteById(id)
            .then(regionSearchRepository.deleteById(id));    }

    @Override
    public Flux<Region> search(String query) {
        log.debug("Request to search Regions for query {}", query);
        return regionSearchRepository.search(query);
    }
}
