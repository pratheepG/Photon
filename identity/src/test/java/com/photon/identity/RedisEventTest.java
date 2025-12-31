package com.photon.identity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

//@Slf4j
//@SpringBootTest
//public class RedisEventTest {
//
//    @Autowired
//    private RedisEvent redisEvent;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Test
//    void testPublishSubscribe() {
//        ChannelTopic topic = new ChannelTopic("test-topic");
//        AtomicBoolean messageReceived = new AtomicBoolean(false);
//
//        MessageListener listener = (message, pattern) -> {
//            log.info("Received message: {}", message);
//            messageReceived.set(true);
//        };
//
//        redisEvent.subscribe(topic, listener);
//        redisEvent.publish(topic, "test-message");
//
//        await().atMost(5, SECONDS).untilTrue(messageReceived);
//        redisEvent.unsubscribe(topic, listener);
//    }
//}