package com.jhipsterdemo.company.service.impl;

import com.jhipsterdemo.company.service.TaskService;
import com.jhipsterdemo.company.domain.Task;
import com.jhipsterdemo.company.repository.TaskRepository;
import com.jhipsterdemo.company.repository.search.TaskSearchRepository;
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
 * Service Implementation for managing {@link Task}.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;

    private final TaskSearchRepository taskSearchRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskSearchRepository taskSearchRepository) {
        this.taskRepository = taskRepository;
        this.taskSearchRepository = taskSearchRepository;
    }

    @Override
    public Mono<Task> save(Task task) {
        log.debug("Request to save Task : {}", task);
        return taskRepository.save(task)
            .flatMap(taskSearchRepository::save)
;    }

    @Override
    public Flux<Task> findAll() {
        log.debug("Request to get all Tasks");
        return taskRepository.findAll();
    }


    public Mono<Long> countAll() {
        return taskRepository.count();
    }

    public Mono<Long> searchCount() {
        return taskSearchRepository.count();
    }

    @Override
    public Mono<Task> findOne(String id) {
        log.debug("Request to get Task : {}", id);
        return taskRepository.findById(id);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Task : {}", id);
        return taskRepository.deleteById(id)
            .then(taskSearchRepository.deleteById(id));    }

    @Override
    public Flux<Task> search(String query) {
        log.debug("Request to search Tasks for query {}", query);
        return taskSearchRepository.search(query);
    }
}
