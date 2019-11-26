package com.noelbundick.comparisons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.tools.agent.ReactorDebugAgent;

@SpringBootApplication
public class Application {

    @Value("${spring.reactor.debug-agent.enabled:true}")
    private static boolean EnableDebugAgent;

    public static void main(String[] args) {
        if (EnableDebugAgent) {
            ReactorDebugAgent.init();
        }

        SpringApplication.run(Application.class, args);
    }

}
