package com.noelbundick.comparisons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class StorageRouter {
    @Value("${AZURE_STORAGE_CONN_STRING}")
    private String storageConnString;

    @Bean
    public RouterFunction<ServerResponse> storage() {
        StorageHandler handler = new StorageHandler(storageConnString);

        return route()
            .path("storage", builder -> builder
                .GET("containers", handler::listContainers)
                .GET("blobs", handler::listAllBlobs)
            ).build();
    }
}
