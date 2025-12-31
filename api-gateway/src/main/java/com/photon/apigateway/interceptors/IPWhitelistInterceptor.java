//package com.photon.apigateway.interceptors;
//
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Component
//public class IPWhitelistInterceptor implements WebFilter {
//
//    private static final List<String> ALLOWED_IPS = Arrays.asList(
//            "127.0.0.1", // Localhost
//            "192.168.1.100", // Example internal IP
//            "192.168.1.101"  // Add more allowed IPs here
//    );
//
//    /**
//     * Process the Web request and (optionally) delegate to the next
//     * {@code WebFilter} through the given {@link WebFilterChain}.
//     *
//     * @param exchange the current server exchange
//     * @param chain    provides a way to delegate to the next filter
//     * @return {@code Mono<Void>} to indicate when request processing is complete
//     */
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        String remoteAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
//
//        if (!ALLOWED_IPS.contains(remoteAddress)) {
//            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
//            return exchange.getResponse().setComplete();
//        }
//
//        return chain.filter(exchange);
//    }
//}