//package com.photon.apigateway;
//
//import com.photon.apigateway.cache.SelectiveCachingRouteLocator;
//import com.photon.apigateway.service.RouteService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
//import org.springframework.cloud.gateway.route.RouteDefinition;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static reactor.core.publisher.Mono.when;
//
//class RouteServiceTest {
//
//    private InMemoryRouteDefinitionRepository repo;
//    private SelectiveCachingRouteLocator selectiveLocator;
//    private RouteService routeService;
//
//    @BeforeEach
//    void setup() {
//        repo = Mockito.mock(InMemoryRouteDefinitionRepository.class);
//        selectiveLocator = Mockito.mock(SelectiveCachingRouteLocator.class);
//        routeService = new RouteService(repo, selectiveLocator);
//
//        // Default stubbing
//        when(repo.delete(any())).thenReturn(Mono.empty());
//        when(repo.save(any())).thenReturn(Mono.empty());
//        when(selectiveLocator.refreshAll()).thenReturn(Mono.empty());
//        when(selectiveLocator.evict(any())).thenReturn(Mono.empty());
//    }
//
//    @Test
//    void testUpdateRoute_ShouldRefreshCache() {
//        // given
//        RouteDefinition rd = new RouteDefinition();
//        rd.setId("route-123");
//
//        // when
//        Mono<Void> result = routeService.updateRoute(rd);
//
//        // then
//        StepVerifier.create(result)
//                .verifyComplete();
//
//        // verify interactions
//        verify(repo, times(1)).delete(any());
//        verify(repo, times(1)).save(any());
//        verify(selectiveLocator, times(1)).refreshAll(); // expect refresh call
//    }
//
//    @Test
//    void testDeleteRoute_ShouldEvictFromCache() {
//        // given
//        String routeId = "route-123";
//
//        // when
//        Mono<Void> result = routeService.deleteRoute(routeId);
//
//        // then
//        StepVerifier.create(result)
//                .verifyComplete();
//
//        verify(repo, times(1)).delete(any());
//        verify(selectiveLocator, times(1)).evict(routeId);
//    }
//
//    @Test
//    void testRefreshAll_ShouldTriggerLocatorRefresh() {
//        // when
//        Mono<Void> result = routeService.refreshAll();
//
//        // then
//        StepVerifier.create(result)
//                .verifyComplete();
//
//        verify(selectiveLocator, times(1)).refreshAll();
//    }
//}