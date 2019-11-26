package com.noelbundick.comparisons.search;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

public class ElasticsearchHandler implements SearchHandler {
    @Override
    public Mono<ServerResponse> errorHandling(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> indexing(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> bulkIndexing(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> clear(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> listDocuments(ServerRequest request) {
        return ok().bodyValue("elastic");
    }

    @Override
    public Mono<ServerResponse> search(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> searchWithPaging(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> searchWithFacets(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> searchWithCustomTypes(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> indexManagement(ServerRequest request) {
        return null;
    }

    @Override
    public Mono<ServerResponse> reset(ServerRequest request) {
        return null;
    }
}
