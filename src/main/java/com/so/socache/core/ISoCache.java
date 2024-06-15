package com.so.socache.core;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-15
 */
public interface ISoCache {

    String get(String key);

    void set(String key, String value);

    int del(String... keys);

    int exists(String... keys);

    String[] mget(String... keys);

    void mset(String[] keys, String[] values);

    int incr(String key);

    int decr(String key);
}
