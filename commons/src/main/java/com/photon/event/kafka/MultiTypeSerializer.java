package com.photon.event.kafka;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class MultiTypeSerializer implements Serializer<Object> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MultiTypeSerializer() {}

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public byte[] serialize(String topic, Object data) {
        try {
            if (data instanceof Message) {
                return ((Message) data).toByteArray();
            } else {
                return objectMapper.writeValueAsBytes(data);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in serialization", e);
        }
    }

    @Override
    public void close() {}
}