package com.photon.apigateway.utils;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Component
public class LoadBalancedResourceRetriever implements ResourceRetriever {

    private final WebClient webClient;
    private static final String REAL_JWKS_URL = "lb://IDENTITY/.well-known/jwks.json";

    public LoadBalancedResourceRetriever(@LoadBalanced WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @Override
    public Resource retrieveResource(URL ignoredUrl) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> future = executor.submit(() ->
                    webClient.get()
                            .uri(REAL_JWKS_URL)
                            .retrieve()
                            .bodyToMono(String.class)
                            .block()
            );

            String jwks = future.get();
            return new Resource(jwks, "application/json; charset=UTF-8");

        } catch (Exception e) {
            log.error("Failed to retrieve JWKS from {}: {}", REAL_JWKS_URL, e.getMessage(), e);
            throw new IOException("Failed to retrieve JWKS from " + REAL_JWKS_URL, e);
        } finally {
            executor.shutdown();
        }
    }

}