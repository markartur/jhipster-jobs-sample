package com.jhipsterdemo.company.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link AccommodationSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class AccommodationSearchRepositoryMockConfiguration {

    @MockBean
    private AccommodationSearchRepository mockAccommodationSearchRepository;

}
