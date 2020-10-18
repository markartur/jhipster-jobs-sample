package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.JobHistoryService;
import com.jhipsterdemo.company.domain.JobHistory;
import com.jhipsterdemo.company.repository.JobHistoryRepository;
import com.jhipsterdemo.company.repository.search.JobHistorySearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Service Implementation for managing {@link JobHistory}.
 */
@Service
public class JobHistoryServiceImpl implements JobHistoryService {

    private final Logger log = LoggerFactory.getLogger(JobHistoryServiceImpl.class);

    private final JobHistoryRepository jobHistoryRepository;

    private final JobHistorySearchRepository jobHistorySearchRepository;

    public JobHistoryServiceImpl(JobHistoryRepository jobHistoryRepository, JobHistorySearchRepository jobHistorySearchRepository) {
        this.jobHistoryRepository = jobHistoryRepository;
        this.jobHistorySearchRepository = jobHistorySearchRepository;
    }

    @Override
    public Mono<JobHistory> save(JobHistory jobHistory) {
        log.debug("Request to save JobHistory : {}", jobHistory);
        return jobHistoryRepository.save(jobHistory)
            .flatMap(jobHistorySearchRepository::save)
;    }

    @Override
    public Flux<JobHistory> findAll(Pageable pageable) {
        log.debug("Request to get all JobHistories");
        return jobHistoryRepository.findAllBy(pageable);
    }


    public Mono<Long> countAll() {
        return jobHistoryRepository.count();
    }

    public Mono<Long> searchCount() {
        return jobHistorySearchRepository.count();
    }

    @Override
    public Mono<JobHistory> findOne(String id) {
        log.debug("Request to get JobHistory : {}", id);
        return jobHistoryRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete JobHistory : {}", id);
        return jobHistoryRepository.deleteById(id)
            .then(jobHistorySearchRepository.deleteById(id));    }

    @Override
    public Flux<JobHistory> search(String query, Pageable pageable) {
        log.debug("Request to search for a page of JobHistories for query {}", query);
        return jobHistorySearchRepository.search(query, pageable);    }
}
