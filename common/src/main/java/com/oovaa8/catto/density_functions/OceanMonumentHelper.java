package com.oovaa8.catto.density_functions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class OceanMonumentHelper {
    //Should be good enough cause I doubt we will ever even be loading two monuments at once
    public static Map<Long, Integer> offset = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>(16, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Integer> eldest) {
            return size() > 500;
        }
    });

    public static long key(int x, int y) {
        return (((long)x) << 32) | (y & 0xffffffffL);
    }
}