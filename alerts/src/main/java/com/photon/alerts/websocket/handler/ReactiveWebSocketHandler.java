package com.photon.alerts.websocket.handler;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ReactiveWebSocketHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<String> messageStream = Flux.interval(Duration.ofSeconds(1))
                .map(interval -> "Message: " + interval);

        return session.send(messageStream.map(session::textMessage));
    }
}