package com.noelbundick.comparisons.search;

import com.azure.search.ApiKeyCredentials;
import com.azure.search.SearchIndexAsyncClient;
import com.azure.search.SearchServiceAsyncClient;
import com.azure.search.SearchServiceClientBuilder;
import com.azure.search.models.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noelbundick.comparisons.search.models.AzureHotel;
import com.noelbundick.comparisons.search.models.HotelAddress;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class AzureSearchHandler implements SearchHandler {
    private final SearchServiceAsyncClient serviceClient;
    private final SearchIndexAsyncClient indexClient;

    public AzureSearchHandler(String endpoint, String adminKey) {
        serviceClient = new SearchServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new ApiKeyCredentials(adminKey))
            .buildAsyncClient();

        indexClient = serviceClient.getIndexClient("hotels");
    }

    @Override
    // Trying to index undefined properties will throw a 400 bad request from the service that we need to handle
    public Mono<ServerResponse> errorHandling(ServerRequest request) {
        Map<String, Object> bogusDocument = new HashMap<>();
        bogusDocument.put("nonExistentKey", "foo");
        List<Map<String, Object>> documents = Collections.singletonList(bogusDocument);

        return indexClient.uploadDocuments(documents)
            .flatMap(res -> ok().bodyValue(String.format("Indexed %d documents", res.getResults().size())))
            .onErrorResume(err -> ServerResponse.status(500).bodyValue("Unexpected error: " + err.toString()));
    }

    @Override
    // There are convenience methods for upload/merge/mergeOrUpload/delete
    public Mono<ServerResponse> indexing(ServerRequest request) {
        AzureHotel hotel = new AzureHotel()
            .hotelId(UUID.randomUUID().toString())
            .address(new HotelAddress().city("Seattle"));
        List<AzureHotel> documents = Collections.singletonList(hotel);

        return indexClient.uploadDocuments(documents)
            .flatMap(res -> ok().bodyValue(String.format("Indexed %d documents", res.getResults().size())));
    }

    @Override
    // All indexing operations flow through an IndexBatch<T> that are sent to a single endpoint
    public Mono<ServerResponse> bulkIndexing(ServerRequest request) {
        IndexBatch<AzureHotel> batch = new IndexBatch<>();
        for (int i = 0; i < 10000; i++) {
            batch.addUploadAction(new AzureHotel()
                .hotelId(Integer.toString(i))
                .address(new HotelAddress().city("Seattle"))
            );
        }

        return indexClient.index(batch)
            .flatMap(res -> ok().bodyValue(String.format("Indexed %d documents", res.getResults().size())));
    }

    @Override
    // Retrieve all documents and delete them by id
    public Mono<ServerResponse> clear(ServerRequest request) {
        return indexClient.search("*")
            .map(res -> (String) res.getDocument().get("HotelId"))
            .collectList()
            .flatMap(ids -> {
                IndexBatch<?> batch = new IndexBatch<>();
                batch.addDeleteAction("HotelId", ids);
                return indexClient.index(batch);
            })
            .map(res -> res.getResults().size())
            .onErrorReturn(0)
            .flatMap(deleted -> ok().bodyValue(String.format("%d documents deleted", deleted)));
    }

    @Override
    // Get a count of all documents in the index
    public Mono<ServerResponse> count(ServerRequest request) {
        Mono<Long> count = indexClient.getDocumentCount();
        return ok().body(count, Long.class);
    }

    @Override
    // Simple term search
    public Mono<ServerResponse> search(ServerRequest request) {
        Flux<SearchResult> results = indexClient.search("Seattle");
        return ok().body(results, SearchResult.class);
    }

    @Override
    // Page sizes are effectively either 50 or 1000:
    // Search defaults to a page size of 50
    // If top is specified and there are more than 1000 results, they are paged 1000 at a time
    public Mono<ServerResponse> searchWithPaging(ServerRequest request) {

        SearchOptions options = new SearchOptions();
        request.queryParam("top").ifPresent(topStr -> {
            options.setTop(Integer.parseInt(topStr));
        });

        Flux<SearchResult> results = indexClient.search("Seattle", options, new RequestOptions());
        return ok().body(results, SearchResult.class);
    }

    @Override
    // Interact with pages to retrieve facet information
    public Mono<ServerResponse> searchWithFacets(ServerRequest request) {
        SearchOptions options = new SearchOptions()
            .setFacets("Category", "Rating", "ParkingIncluded", "Rooms/SmokingAllowed");

        Mono<HashMap<String, HashMap<String, Long>>> results = indexClient.search("*", options, new RequestOptions())
            .byPage()
            .single()
            .map(page -> {
                HashMap<String, HashMap<String, Long>> facetResults = new HashMap<>();
                for (Map.Entry<String, List<FacetResult>> entry : page.facets().entrySet()) {
                    HashMap<String, Long> facetValues = new HashMap<>();
                    for (FacetResult facetResult : entry.getValue()) {
                        String value = facetResult.getDocument().get("value").toString();
                        Long count = facetResult.getCount();
                        facetValues.put(value, count);
                    }
                    facetResults.put(entry.getKey(), facetValues);
                }
                return facetResults;
            });

        return ok().body(results, new ParameterizedTypeReference<>() {});
    }

    @Override
    // Bring-your-own custom marshaling with a properly configured mapper
    public Mono<ServerResponse> searchWithCustomTypes(ServerRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Flux<AzureHotel> results = indexClient.search("Seattle")
            .take(5)
            .map(result -> mapper.convertValue(result.getDocument(), AzureHotel.class));
        return ok().body(results, AzureHotel.class);
    }

    @Override
    // Drop and recreate an index
    public Mono<ServerResponse> indexManagement(ServerRequest request) {
        String indexName = "simple";
        Index simpleIndex = new Index()
            .setName(indexName)
            .setFields(Arrays.asList(
                new Field()
                    .setName("foo")
                    .setKey(true)
                    .setType(DataType.EDM_STRING)
                    .setSearchable(true)
                    .setSortable(true),
                new Field()
                    .setName("bar")
                    .setType(DataType.EDM_INT32)
                    .setFacetable(true)
                    .setSortable(true)
            ));

        Mono<Index> result = serviceClient.deleteIndex(indexName)
            .onErrorResume(__ -> Mono.empty())
            .then(serviceClient.createIndex(simpleIndex));
        return ok().body(result, Index.class);
    }

    @Override
    // Clear an index and re-run an indexer to import from CosmosDB
    public Mono<ServerResponse> reset(ServerRequest request) {
        String indexerName = request.queryParam("indexer")
            .orElse("sample-indexer");

        return clearDocuments()
            .then(populateIndex(indexerName))
            .then(ok().bodyValue("reset"));
    }

    private Mono<Integer> clearDocuments() {
        return indexClient.search("*")
            .map(SearchResult::getDocument)
            .collectList()
            .flatMap(indexClient::deleteDocuments)
            .map(res -> res.getResults().size())
            .onErrorReturn(0);
    }

    private Mono<Void> populateIndex(String indexerName) {
        return serviceClient.resetIndexerWithResponse(indexerName, new RequestOptions())
            .then(serviceClient.runIndexer(indexerName));
    }
}
