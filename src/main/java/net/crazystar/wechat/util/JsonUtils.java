package net.crazystar.wechat.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Slf4j
public class JsonUtils {
    private static final Gson GSON = new Gson();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};
    private static final TypeReference<Set<String>> SET_TYPE = new TypeReference<Set<String>>() {};
    public static Map<String, Object> toMap(String json) {
        if (json == null) {
            return Collections.emptyMap();
        }
        try {
            return MAPPER.readValue(json, MAP_TYPE);
        } catch (Exception e) {
            log.error("parse json error.", e);
        }
        return Collections.emptyMap();
    }

    public static Set<String> toSet(String json) {
        try {
            return MAPPER.readValue(json, SET_TYPE);
        } catch (Exception e) {
            log.error("parse json error.", e);
        }
        return Collections.emptySet();
    }

    public static String toJson(Object o) {
        return GSON.toJson(o);
    }
}
