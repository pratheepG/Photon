package com.photon.identity.authentication.event.listener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PostStartupRunner {

//    private final RedisEvent redisEvent;
//
//    public PostStartupRunner(RedisEvent redisEvent) {
//        this.redisEvent = redisEvent;
//    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("Application is ready. Performing post-startup tasks...");

//        this.redisEvent.subscribe(new PatternTopic("__keyevent@0__:expired"), (message, topic)->{
//            String expiredKey = message.toString();
//            if (expiredKey.startsWith("login:")) {
//                // Extract userId and requestId from key
//                String[] parts = expiredKey.split(":");
//                String userId = parts[1];
//                String requestId = parts[2];
//
//                // Update MySQL asynchronously
//                // updateLoginHistory(userId, requestId, LoginStep.EXPIRED);
//            }
//        });
    }

}