package com.noelbundick.comparisons;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Configuration
public class HelloRouter {
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
}
