package com.noelbundick.comparisons;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloTests {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testHello() {
        webTestClient.get().uri("/hello")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello, Spring!");
    }

    @Test
    public void testHelloWithName() {
        webTestClient.get().uri("/hello/World")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello, World!");
    }
}
