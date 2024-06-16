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

    /**
     * 该函数用于检查给定的字符串数组中是否存在键值在map中，返回存在的键值个数。如果字符串数组为null，则返回0。
     * 首先，函数使用Arrays.stream(keys)将字符串数组转换为流（Stream）。
     * 然后，通过map::containsKey方法将每个键值映射为一个布尔值，表示该键值是否在map中存在。
     * 接着，使用filter(x -> x)过滤掉值为false的布尔值，只保留值为true的布尔值。
     * 最后，使用count()方法统计剩余的布尔值个数，即存在的键值个数，并将其强制转换为int类型后返回。
     * @param keys
     * @return
     */
    public int exists(String... keys) {
        return keys == null ? 0: (int) Arrays.stream(keys)
                .map(map::containsKey)
                .filter(x -> x) //
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

    /**
     * 该函数用于对指定key的值进行自增操作。
     * 首先通过get方法获取key对应的字符串值，如果字符串不为空，则将其转换为整数类型并自增1，
     * 然后通过set方法将更新后的值保存回去。如果字符串为空或无法转换为整数，
     * 则会抛出NumberFormatException异常。
     * 最后返回更新后的值
     * @param key
     * @return
     */
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
