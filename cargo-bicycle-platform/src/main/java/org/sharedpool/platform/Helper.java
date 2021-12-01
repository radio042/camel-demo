package org.sharedpool.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class Helper {

    public static String toJson(Map<String, ?> input) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "error, see logs";
        }
    }

    public static Map<String, Object> toMap(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Map.of();
        }
    }
}
