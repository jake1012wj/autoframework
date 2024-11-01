package org.dromara.sms4j.provider.config;


import lombok.Data;
import org.dromara.sms4j.comm.enums.ConfigType;

import java.util.ArrayList;

/**
 * 短信配置类
 */
@Data
public class SmsConfig {

    /** 配置源类型*/
    private ConfigType configType = ConfigType.YAML;

    /**
     * 打印banner
     */
    private Boolean isPrint = true;

    /**
     * 是否开启短信限制
     */
    private Boolean restricted = false;

    /**
     * 单账号每日最大发送量
     */
    private Integer accountMax;

    /**
     * 单账号每分钟最大发送
     */
    private Integer minuteMax;

    /**
     * 核心线程池大小
     */
    private Integer corePoolSize = 10;

    /**
     * 最大线程数
     */
    private Integer maxPoolSize = 30;

    /**
     * 队列容量
     */
    private Integer queueCapacity = 50;

    /**
     * 设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
     */
    private Boolean shutdownStrategy = true;

    /** 是否打印http log*/
    private Boolean HttpLog = false;

    /**
     * 黑名单配置 此配置不支持yaml读取
     */
    private ArrayList<String> blackList = new ArrayList<>();

}
