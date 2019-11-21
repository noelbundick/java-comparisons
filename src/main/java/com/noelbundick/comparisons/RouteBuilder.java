package com.noelbundick.comparisons;

import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Uses a type to generate GET methods for each of its methods
 */
public class RouteBuilder<T> {
    private Class<T> clazz;

    public RouteBuilder(Class<T> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings("unchecked")
    public RouterFunctions.Builder buildRoutes(RouterFunctions.Builder builder, T handler) {
        Method[] methods = clazz.getMethods();

        for (Method m : methods) {
            builder.GET(m.getName(), req -> {
                try {
                    return Optional.ofNullable((Mono<ServerResponse>)m.invoke(handler, req))
                        .orElse(ServerResponse.status(500).bodyValue("Not implemented"))
                        .onErrorResume(e -> ServerResponse.status(500)
                            .bodyValue(String.format("There was an unexpected error: %s", e)));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
        return builder;
    }
}
