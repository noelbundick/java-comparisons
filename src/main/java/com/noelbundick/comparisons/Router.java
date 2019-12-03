package com.noelbundick.comparisons;

import com.noelbundick.comparisons.search.AzureSearchHandler;
import com.noelbundick.comparisons.search.ElasticsearchHandler;
import com.noelbundick.comparisons.search.SearchHandler;
import com.noelbundick.comparisons.storage.AzureStorageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class Router {
    @Value("${AZURE_STORAGE_CONN_STRING}")
    private String storageConnString;

    @Value("${AZURE_SEARCH_ENDPOINT}")
    private String searchEndpoint;

    @Value("${AZURE_SEARCH_KEY}")
    private String searchAdminKey;

    @Value("${ELASTICSEARCH_HOST}")
    private String elasticsearchHost;

    @Bean
    public RouterFunction<ServerResponse> hello() {
        return route()
            .path("/hello", builder -> builder
                .GET("", request -> ok().body(fromValue("Hello, Spring!")))
                .GET("/{name}", request -> {
                    String name = request.pathVariable("name");
                    String message = String.format("Hello, %s!", name);
                    return ok().body(fromValue(message));
                })
            )
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> search() {
        RouteBuilder<SearchHandler> routeBuilder = new RouteBuilder<>(SearchHandler.class);
        return route()
            .path("search/azure",
                builder -> routeBuilder.buildRoutes(builder, new AzureSearchHandler(searchEndpoint, searchAdminKey)))
            .path("search/elasticsearch",
                builder -> routeBuilder.buildRoutes(builder, new ElasticsearchHandler(elasticsearchHost)))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> storage() {
        AzureStorageHandler handler = new AzureStorageHandler(storageConnString);

        return route()
            .path("storage", builder -> builder
                .GET("containers", handler::listContainers)
                .GET("blobs", handler::listAllBlobs)
            ).build();
    }
}
