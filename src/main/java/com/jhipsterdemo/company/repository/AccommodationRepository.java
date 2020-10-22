package com.jhipsterdemo.company.repository;

import com.jhipsterdemo.company.domain.Accommodation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB reactive repository for the Accommodation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AccommodationRepository extends ReactiveMongoRepository<Accommodation, String> {


}
