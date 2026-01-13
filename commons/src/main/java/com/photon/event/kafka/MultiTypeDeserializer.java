package com.photon.event.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import com.photon.alerts.enums.Priority;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.HashMap;
import java.util.Map;

public class MultiTypeDeserializer implements Deserializer<Object> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Map<String, Class<? extends Message>> PROTOBUF_TYPE_MAP = new HashMap<>();

    static {
        PROTOBUF_TYPE_MAP.put(Priority.HIGH.getBrokerTopic(), com.photon.alerts.proto.AlertRequest.class);
        PROTOBUF_TYPE_MAP.put(Priority.MEDIUM_HIGH.getBrokerTopic(), com.photon.alerts.proto.AlertRequest.class);
        PROTOBUF_TYPE_MAP.put(Priority.MEDIUM.getBrokerTopic(), com.photon.alerts.proto.AlertRequest.class);
        PROTOBUF_TYPE_MAP.put(Priority.MEDIUM_LOW.getBrokerTopic(), com.photon.alerts.proto.AlertRequest.class);
        PROTOBUF_TYPE_MAP.put(Priority.LOW.getBrokerTopic(), com.photon.alerts.proto.AlertRequest.class);
    }

    public MultiTypeDeserializer() {}

    @Override
    public Object deserialize(String topic, byte[] data) {
        if (PROTOBUF_TYPE_MAP.containsKey(topic)) {
            try {
                return PROTOBUF_TYPE_MAP.get(topic)
                        .getMethod("parseFrom", byte[].class)
                        .invoke(null, (Object) data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize Protobuf message from topic: " + topic, e);
            }
        }

        try {
            return objectMapper.readValue(data, String.class);
        } catch (Exception jsonException) {
            throw new RuntimeException("Unknown message format from topic: " + topic, jsonException);
        }
    }

    @Override
    public void close() {}
}