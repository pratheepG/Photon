package com.photon.identity.authentication.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.photon.identity.authentication.dto.request.*;

import java.io.IOException;

public class AuthRequestDeserializer extends JsonDeserializer<AbstractAuthRequest> {

    @Override
    public AbstractAuthRequest deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        ObjectNode node = mapper.readTree(parser);

        if (node.has("token")) {
            return mapper.readValue(node.toString(), LoginOAuthDto.class);
        }

        if (node.has("principle")) {
            return mapper.readValue(node.toString(), LoginOtpDto.class);
        }

        if (node.has("password") && node.has("securityKey")) {
            return mapper.readValue(node.toString(), VerifyOtpDto.class);
        }

        if (node.has("userName") && node.has("password")) {
            return mapper.readValue(node.toString(), LoginStaticPasswordDto.class);
        }

        throw new IllegalArgumentException("Unsupported payload type");
    }
}