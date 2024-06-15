package com.so.socache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * plugins entrypoint
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-06-12
 */
@Component
@Slf4j
public class SoCacheApplicationListener implements ApplicationListener<ApplicationEvent> {

    @Autowired
    List<SoCachePlugin> plugins;
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.info("SoCacheApplicationListener.onApplicationEvent: {}", event);
        // ApplicationReadyEvent
        if (event instanceof ApplicationReadyEvent are) {
            //plugin init and startup
            for (SoCachePlugin plugin : plugins) {
                plugin.init();
                plugin.startup();
            }
        } else if (event instanceof ContextClosedEvent cce) {
            for (SoCachePlugin plugin : plugins) {
                plugin.shutdown();
            }
        }
    }
}
