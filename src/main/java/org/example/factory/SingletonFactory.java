package org.example.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {

    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory(){}

    public static <T> T getInstance(Class<T> c){
        if (c == null){
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        return c.cast(OBJECT_MAP.computeIfAbsent(key, k -> {
            try {
                return c.getDeclaredConstructor().newInstance();
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }));
    }
}
