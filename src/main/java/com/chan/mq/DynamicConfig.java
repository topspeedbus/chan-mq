package com.chan.mq;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.file.*;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author: chen
 * @date: 2020/6/9 - 11:39
 * @describe:
 */
@Component
public class DynamicConfig {

    static final String fileName;
    static final ClassPathResource classPathResource;
    static WatchService watchService;
    static Properties properties;
    static final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("config-watch-thread");
        thread.setDaemon(true);
        return thread;
    });

    static {
        fileName = "diy-config.properties";
        classPathResource = new ClassPathResource(fileName);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Paths.get(classPathResource.getFile().getParent())
                    .register(watchService,
                            StandardWatchEventKinds.ENTRY_MODIFY);
            properties = PropertiesLoaderUtils.loadProperties(classPathResource);
            executorService.execute(() -> {
                while (true) {
                    WatchKey watchKey = null;
                    try {
                        watchKey = watchService.take();
                        for (WatchEvent event : watchKey.pollEvents()) {
                            if (Objects.equals(event.context().toString(), fileName)) {
                                properties = PropertiesLoaderUtils.loadProperties(classPathResource);
                                break;
                            }
                        }
                        watchKey.reset();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @PostConstruct
    public void test() throws InterruptedException {
        Object o = properties.get("test.me");
        System.out.println(o);

        Thread.sleep(30000);

        Object o1 = properties.get("test.me");
        System.out.println(o1);
    }
}
