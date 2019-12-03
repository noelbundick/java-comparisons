package com.noelbundick.comparisons.search;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

public interface SearchHandler {
    // Cross-cutting concerns
    Mono<ServerResponse> errorHandling(ServerRequest request);

    // Getting data into the service
    Mono<ServerResponse> indexing(ServerRequest request);
    Mono<ServerResponse> bulkIndexing(ServerRequest request);
    Mono<ServerResponse> clear(ServerRequest request);

    // Retrieving data
    Mono<ServerResponse> count(ServerRequest request);
    Mono<ServerResponse> search(ServerRequest request);
    Mono<ServerResponse> searchWithPaging(ServerRequest request);
    Mono<ServerResponse> searchWithFacets(ServerRequest request);
    Mono<ServerResponse> searchWithCustomTypes(ServerRequest request);

    // Management of entities within the service
    Mono<ServerResponse> indexManagement(ServerRequest request);

    // Reset to base state
    Mono<ServerResponse> reset(ServerRequest request);
}
