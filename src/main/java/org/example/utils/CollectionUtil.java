package org.example.utils;

import java.util.Collection;
public final class CollectionUtil {

    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }
}
