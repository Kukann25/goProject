package project.go.server.common.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFmt {
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Converts an object to its JSON string representation */
    synchronized public static String toJson(final Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    /** Parses a JSON string to create an object of the specified class */
    synchronized public static <T> T fromJson(final String json, final Class<T> cls) throws JsonProcessingException {
        return mapper.readValue(json, cls);
    }

    /** Parses a JSON string into a JsonNode for inspection */
    synchronized public static JsonNode readTree(final String json) throws JsonProcessingException {
        return mapper.readTree(json);
    }

    /** Converts a JsonNode to a specific class */
    synchronized public static <T> T treeToValue(final JsonNode node, final Class<T> cls) throws JsonProcessingException {
        return mapper.treeToValue(node, cls);
    }
}
