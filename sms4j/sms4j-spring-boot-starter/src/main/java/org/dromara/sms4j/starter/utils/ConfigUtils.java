package org.dromara.sms4j.starter.utils;

import org.springframework.core.env.Environment;

/**
 * ConfigUtil
 * <p> 读取配置文件工具
 * @author :Wind
 * 2023/4/7  21:39
 **/
public class ConfigUtils {

    private final Environment environment;

    public ConfigUtils(Environment environment) {
        this.environment = environment;
    }

    public String getValue(String key) {
        return environment.getProperty(key);
    }

    public <T>T getValue(String key, Class<T> type){
        return environment.getProperty(key,type);
    }

}
