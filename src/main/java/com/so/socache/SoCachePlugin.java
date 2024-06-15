package com.so.socache;

/**
 * so cache plugin
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-12
 */
public interface SoCachePlugin {

    void init();
    void startup();
    void shutdown();
}
