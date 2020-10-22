package com.jhipsterdemo.company.web.rest;

import com.jhipsterdemo.company.domain.Accommodation;
import com.jhipsterdemo.company.service.AccommodationService;
import com.jhipsterdemo.company.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.reactive.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.jhipsterdemo.company.domain.Accommodation}.
 */
@RestController
@RequestMapping("/api")
public class AccommodationResource {

    private final Logger log = LoggerFactory.getLogger(AccommodationResource.class);

    private static final String ENTITY_NAME = "accommodation";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AccommodationService accommodationService;

    public AccommodationResource(AccommodationService accommodationService) {
        this.accommodationService = accommodationService;
    }

    /**
     * {@code POST  /accommodations} : Create a new accommodation.
     *
     * @param accommodation the accommodation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new accommodation, or with status {@code 400 (Bad Request)} if the accommodation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/accommodations")
    public Mono<ResponseEntity<Accommodation>> createAccommodation(@RequestBody Accommodation accommodation) throws URISyntaxException {
        log.debug("REST request to save Accommodation : {}", accommodation);
        if (accommodation.getId() != null) {
            throw new BadRequestAlertException("A new accommodation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return accommodationService.save(accommodation)
            .map(result -> {
                try {
                    return ResponseEntity.created(new URI("/api/accommodations/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /accommodations} : Updates an existing accommodation.
     *
     * @param accommodation the accommodation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated accommodation,
     * or with status {@code 400 (Bad Request)} if the accommodation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the accommodation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/accommodations")
    public Mono<ResponseEntity<Accommodation>> updateAccommodation(@RequestBody Accommodation accommodation) throws URISyntaxException {
        log.debug("REST request to update Accommodation : {}", accommodation);
        if (accommodation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        return accommodationService.save(accommodation)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map(result -> ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
                .body(result)
            );
    }

    /**
     * {@code GET  /accommodations} : get all the accommodations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of accommodations in body.
     */
    @GetMapping("/accommodations")
    public Mono<List<Accommodation>> getAllAccommodations() {
        log.debug("REST request to get all Accommodations");
        return accommodationService.findAll().collectList();
    }

    /**
     * {@code GET  /accommodations} : get all the accommodations as a stream.
     * @return the {@link Flux} of accommodations.
     */
    @GetMapping(value = "/accommodations", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<Accommodation> getAllAccommodationsAsStream() {
        log.debug("REST request to get all Accommodations as a stream");
        return accommodationService.findAll();
    }

    /**
     * {@code GET  /accommodations/:id} : get the "id" accommodation.
     *
     * @param id the id of the accommodation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the accommodation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/accommodations/{id}")
    public Mono<ResponseEntity<Accommodation>> getAccommodation(@PathVariable String id) {
        log.debug("REST request to get Accommodation : {}", id);
        Mono<Accommodation> accommodation = accommodationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(accommodation);
    }

    /**
     * {@code DELETE  /accommodations/:id} : delete the "id" accommodation.
     *
     * @param id the id of the accommodation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/accommodations/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteAccommodation(@PathVariable String id) {
        log.debug("REST request to delete Accommodation : {}", id);
        return accommodationService.delete(id)            .map(result -> ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
        );
    }

    /**
     * {@code SEARCH  /_search/accommodations?query=:query} : search for the accommodation corresponding
     * to the query.
     *
     * @param query the query of the accommodation search.
     * @return the result of the search.
     */
    @GetMapping("/_search/accommodations")
    public Mono<List<Accommodation>> searchAccommodations(@RequestParam String query) {
        log.debug("REST request to search Accommodations for query {}", query);
        return accommodationService.search(query).collectList();
    }
}
