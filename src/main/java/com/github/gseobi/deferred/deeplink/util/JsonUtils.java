package com.github.gseobi.deferred.deeplink.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper OM = new ObjectMapper();

    private JsonUtils() {}

    public static String toJson(Object o) {
        try { return OM.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }

    public static JsonNode readTree(String json) {
        try { return OM.readTree(json); }
        catch (Exception e) { return OM.createObjectNode(); }
    }
}