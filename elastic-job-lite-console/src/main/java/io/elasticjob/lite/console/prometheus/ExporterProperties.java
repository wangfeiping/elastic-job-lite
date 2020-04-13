package io.elasticjob.lite.console.prometheus;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExporterProperties {

    private static ExporterProperties instance = new ExporterProperties();

    private static volatile boolean initial = true;
    
    private Properties props;
    
    /**
     * 获取属性缓存实例.
     * 
     * @return 属性缓存实例
     */
    public static Properties getProperties() {
        if (initial) {
            instance.init();
        }
        return instance.props;
    }
    
    /**
     * 获取属性字符串值.
     * 
     * @param name 属性名称
     * @return 属性字符串值
     */
    public static String getProperty(final String name) {
        if (initial) {
            instance.init();
        }
        return instance.get(name);
    }
    
    /**
     * 获取属性整型值.
     * 
     * @param name 属性名称
     * @param defaultValue 属性默认值
     * @return 属性整型值
     */
    public static int getPropertyInt(
            final String name, final int defaultValue) {
        try {
            return Integer.parseInt(getProperty(name));
        } catch (NumberFormatException e) {
            log.warn("property({}) parse error: {} - {}",
                    name, e.getMessage(), e.getStackTrace());
        }
        return defaultValue;
    }
    
    /**
     * 获取属性布尔值.
     * 
     * @param name 属性名称
     * @return 属性布尔值
     */
    public static boolean getPropertyBoolean(
            final String name) {
        return Boolean.parseBoolean(getProperty(name));
    }
    
    private synchronized void init() {
        if (!initial) {
            return;
        }
        props = new Properties();
        InputStream inputStream = Object.class
                .getResourceAsStream("/conf/exporter.properties");
        try {
            props.load(inputStream);
            initial = false;
        } catch (IOException e) {
            log.error("properties init error: {} - {}",
                    e.getMessage(), e.getStackTrace());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("input stream close error: {} - {}",
                        e.getMessage(), e.getStackTrace());
            }
        }
        log.info("load exporter.properties: exporter.enable: {}",
                get("exporter.enable"));
        log.info("load exporter.properties: exporter.port: {}",
                get("exporter.port"));
        log.info("load exporter.properties: zookeeper.connect: {}",
                get("zookeeper.connect"));
        log.info("load exporter.properties: namespace: {}",
                get("namespace"));
        log.info("load exporter.properties: digest: {}",
                get("digest"));
    }
    
    private String get(final String name) {
        return props.getProperty(name);
    }
}
