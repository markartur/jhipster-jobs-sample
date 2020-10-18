package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.DepartmentService;
import com.jhipsterdemo.company.domain.Department;
import com.jhipsterdemo.company.repository.DepartmentRepository;
import com.jhipsterdemo.company.repository.search.DepartmentSearchRepository;
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
 * Service Implementation for managing {@link Department}.
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;

    private final DepartmentSearchRepository departmentSearchRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DepartmentSearchRepository departmentSearchRepository) {
        this.departmentRepository = departmentRepository;
        this.departmentSearchRepository = departmentSearchRepository;
    }

    @Override
    public Mono<Department> save(Department department) {
        log.debug("Request to save Department : {}", department);
        return departmentRepository.save(department)
            .flatMap(departmentSearchRepository::save)
;    }

    @Override
    public Flux<Department> findAll() {
        log.debug("Request to get all Departments");
        return departmentRepository.findAll();
    }


    public Mono<Long> countAll() {
        return departmentRepository.count();
    }

    public Mono<Long> searchCount() {
        return departmentSearchRepository.count();
    }

    @Override
    public Mono<Department> findOne(String id) {
        log.debug("Request to get Department : {}", id);
        return departmentRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Department : {}", id);
        return departmentRepository.deleteById(id)
            .then(departmentSearchRepository.deleteById(id));    }

    @Override
    public Flux<Department> search(String query) {
        log.debug("Request to search Departments for query {}", query);
        return departmentSearchRepository.search(query);
    }
}
