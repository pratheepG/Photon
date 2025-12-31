package com.photon.apigateway;

import com.photon.apigateway.service.GatewayRouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private GatewayRouteService routeService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(java.time.Duration.ofMillis(30000))
                .build();
    }

    @Test
    void testDynamicRouteUpdate_ShouldServeNewRoute() throws InterruptedException {
        // given a new route definition
        RouteDefinition rd = new RouteDefinition();
        rd.setId("test-route");
        rd.setUri(URI.create("https://httpbin.org"));

        // Use the standard Path predicate with a single argument for the path pattern.
        PredicateDefinition predicate = new PredicateDefinition();
        predicate.setName("Path");
        predicate.addArg("_genkey_0", "/test/**");
        rd.setPredicates(Collections.singletonList(predicate));

        // optional: add a StripPrefix=1 filter
        FilterDefinition filter = new FilterDefinition();
        filter.setName("StripPrefix");
        filter.addArg("_genkey_0", "1");
        rd.setFilters(Collections.singletonList(filter));

        // update route, blocking to ensure the Mono completes
        routeService.updateRoute(rd.getId(), rd).block();

        // The route update is asynchronous due to the event. We must wait for
        // the event to be processed and the routes to be refreshed.
        // Waiting for a slightly longer period is more reliable in tests.
        TimeUnit.MILLISECONDS.sleep(500);

        // then: call through gateway and verify it forwards to httpbin
        webTestClient.get()
                .uri("/test/get")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.url").value(url -> {
                    // The assertion now expects the URL from httpbin.org, proving the gateway
                    // successfully forwarded the request.
                    assertThat(url.toString()).contains("httpbin.org/get");
                });
    }

}