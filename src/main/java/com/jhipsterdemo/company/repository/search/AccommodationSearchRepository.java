package com.jhipsterdemo.company.repository.search;

import com.jhipsterdemo.company.domain.Accommodation;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;


import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * Spring Data Elasticsearch repository for the {@link Accommodation} entity.
 */
public interface AccommodationSearchRepository extends ReactiveElasticsearchRepository<Accommodation, String>, AccommodationSearchRepositoryInternal {
}

interface AccommodationSearchRepositoryInternal {
    Flux<Accommodation> search(String query);
}

class AccommodationSearchRepositoryInternalImpl implements AccommodationSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    AccommodationSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Accommodation> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.find(nativeSearchQuery, Accommodation.class);
    }
}
