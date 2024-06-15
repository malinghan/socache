package com.so.socache.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * define so cache interface
 *
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-15
 */
public class SoCache implements  ISoCache {
    Map<String, String> map = new HashMap<>();

    public String get(String key) {
        return map.get(key);
    }

    public void set(String key, String value) {
        map.put(key, value);
    }

    public int del(String... keys) {
       return keys == null ? 0: (int) Arrays.stream(keys)
               .map(map::remove)
               .filter(Objects::nonNull)
               .count()
               ;
    }

    public int exists(String... keys) {
        return keys == null ? 0: (int) Arrays.stream(keys)
                .map(map::containsKey)
                .filter(Objects::nonNull)
                .count();
    }

    public String[] mget(String... keys) {
        return keys == null ? new String[0] : Arrays.stream(keys)
                .map(map::get)
                .toArray(String[]::new);
    }

    public void mset(String[] keys, String[] values) {
         if (keys == null || keys.length == 0) {
            return;
        }
        for (int i = 0; i < keys.length; i++) {
             set(keys[i], values[i]);
        }
    }

    public int incr(String key) {
        String str = get(key);
        int val = 0;
        try {
            if (str != null) {
                val = Integer.parseInt(str);
            }
            val++;
            set(key, String.valueOf(val));
        } catch (NumberFormatException nfe) {
            throw nfe;
        }
        return val;
    }

    public int decr(String key) {
        String str = get(key);
        int val = 0;
        try {
            if (str != null) {
                val = Integer.parseInt(str);
            }
            val--;
            set(key, String.valueOf(val));
        } catch (NumberFormatException nfe) {
            throw nfe;
        }
        return val;
    }
}
