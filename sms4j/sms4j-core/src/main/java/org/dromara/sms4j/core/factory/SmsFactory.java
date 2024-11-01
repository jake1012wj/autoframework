package org.dromara.sms4j.core.factory;

import cn.hutool.core.util.StrUtil;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.universal.SupplierConfig;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.core.load.SmsLoad;
import org.dromara.sms4j.core.proxy.SmsProxyFactory;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.provider.factory.BaseProviderFactory;
import org.dromara.sms4j.provider.factory.ProviderFactoryHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * SmsFactory
 * <p>构造工厂，用于获取一个厂商的短信实现对象
 * 在调用对应厂商的短信发送方法前，请先确保你的配置已经实现，否则无法发送该厂商对应的短信，一般情况下厂商会回执因缺少的配置所造成的的异常，但组件
 * 不会处理
 *
 * @author :Wind
 * 2023/4/8  15:55
 **/
public abstract class SmsFactory {

    /**
     * <p>框架维护的所有短信服务对象</p>
     * <p>key: configId，短信服务对象的唯一标识</p>
     * <p>value: 短信服务对象</p>
     */
    private final static Map<String, SmsBlend> BLENDS = new ConcurrentHashMap<>();

    private SmsFactory() {
    }

    /**
     * createSmsBlend
     * <p>创建各个厂商的实现类
     *
     * @param config 短信配置
     * @author :Wind
     */
    public static void createSmsBlend(SupplierConfig config) {
        SmsBlend smsBlend = create(config);
        register(smsBlend);
    }


    /**
     * createSmsBlend
     * <p>通过配置读取接口创建某个短信实例
     * <p>该方法创建的短信实例将会交给框架进行托管，后续可以通过getSmsBlend获取
     * <p>该方法会直接调用接口实现
     *
     * @param smsReadConfig 读取额外配置接口
     * @param configId      配置ID
     * @author :Wind
     */
    public static void createSmsBlend(SmsReadConfig smsReadConfig, String configId) {
        BaseConfig supplierConfig = smsReadConfig.getSupplierConfig(configId);
        SmsBlend smsBlend = create(supplierConfig);
        register(smsBlend);
    }

    /**
     * createSmsBlend
     * <p>通过配置读取接口创建全部短信实例
     * <p>该方法创建的短信实例将会交给框架进行托管，后续可以通过getSmsBlend获取
     * <p>该方法会直接调用接口实现
     *
     * @param smsReadConfig 读取额外配置接口
     * @author :Wind
     */
    public static void createSmsBlend(SmsReadConfig smsReadConfig) {
        List<BaseConfig> supplierConfigList = smsReadConfig.getSupplierConfigList();
        supplierConfigList.forEach(supplierConfig -> {
            SmsBlend smsBlend = create(supplierConfig);
            register(smsBlend);
        });
    }

    /**
     * createRestrictedSmsBlend
     * <p> 创建一个指定厂商开启短信拦截后的实例，拦截的参数取决于配置参数
     *
     * @param config 短信配置
     * @author :Wind
     */
    @Deprecated
    public static void createRestrictedSmsBlend(SupplierConfig config) {
        SmsBlend smsBlend = create(config);
        register(smsBlend);
    }

    /**
     * createRestrictedSmsBlend
     * <p>通过配置读取接口创建某个开启短信拦截后的短信实例
     * <p>该方法创建的短信实例将会交给框架进行托管，后续可以通过getSmsBlend获取
     * <p>该方法会直接调用接口实现
     *
     * @param smsReadConfig 读取额外配置接口
     * @param configId      配置ID
     * @author :Wind
     */
    @Deprecated
    public static void createRestrictedSmsBlend(SmsReadConfig smsReadConfig, String configId) {
        BaseConfig supplierConfig = smsReadConfig.getSupplierConfig(configId);
        SmsBlend smsBlend = create(supplierConfig);
        register(smsBlend);
    }

    /**
     * createRestrictedSmsBlend
     * <p>通过配置读取接口创建全部开启短信拦截后的短信实例
     * <p>该方法创建的短信实例将会交给框架进行托管，后续可以通过getSmsBlend获取
     * <p>该方法会直接调用接口实现
     *
     * @param smsReadConfig 读取额外配置接口
     * @author :Wind
     */
    @Deprecated
    public static void createRestrictedSmsBlend(SmsReadConfig smsReadConfig) {
        List<BaseConfig> supplierConfigList = smsReadConfig.getSupplierConfigList();
        supplierConfigList.forEach(supplierConfig -> {
            SmsBlend smsBlend = create(supplierConfig);
            register(smsBlend);
        });
    }

    private static SmsBlend create(SupplierConfig config) {
        BaseProviderFactory factory = ProviderFactoryHolder.requireForSupplier(config.getSupplier());
        if (factory == null) {
            throw new SmsBlendException("不支持当前供应商配置");
        }
        SmsBlend sms = factory.createSms(config);
        return renderWithProxy(sms);

    }


    /**
     * renderWithRestricted
     * <p>  构建smsBlend对象的代理对象
     *
     * @author :Wind
     */
    @Deprecated
    private static SmsBlend renderWithProxy(SmsBlend sms) {
        return SmsProxyFactory.getProxySmsBlend(sms);
    }

    /**
     * 通过负载均衡服务获取短信服务对象
     *
     * @return 返回短信服务列表
     */
    public static SmsBlend getSmsBlend() {
        return SmsLoad.getBeanLoad().getLoadServer();
    }

    /**
     * 通过configId获取短信服务对象
     *
     * @param configId 唯一标识
     * @return 返回短信服务对象。如果未找到则返回null
     */
    public static SmsBlend getSmsBlend(String configId) {
        return BLENDS.get(configId);
    }

    /**
     * 通过供应商标识获取单个短信服务对象
     * <p>当供应商有多个短信服务对象时无法保证获取顺序</p>
     *
     * @param supplier 供应商标识
     * @return 返回短信服务对象。如果未找到则返回null
     */
    public static SmsBlend getBySupplier(String supplier) {
        if (StrUtil.isEmpty(supplier)) {
            throw new SmsBlendException("供应商标识不能为空");
        }
        return BLENDS.values().stream().filter(smsBlend -> supplier.equals(smsBlend.getSupplier())).findFirst().orElse(null);
    }

    /**
     * 通过供应商标识获取短信服务对象列表
     *
     * @param supplier 供应商标识
     * @return 返回短信服务对象列表。如果未找到则返回空列表
     */
    public static List<SmsBlend> getListBySupplier(String supplier) {
        List<SmsBlend> list;
        if (StrUtil.isEmpty(supplier)) {
            throw new SmsBlendException("供应商标识不能为空");
        }
        list = BLENDS.values().stream().filter(smsBlend -> supplier.equals(smsBlend.getSupplier())).collect(Collectors.toList());
        return list;
    }

    /**
     * 获取全部短信服务对象
     *
     * @return 短信服务对象列表
     */
    public static List<SmsBlend> getAll() {
        return new ArrayList<>(BLENDS.values());
    }

    /**
     * 注册短信服务对象
     *
     * @param smsBlend 短信服务对象
     */
    public static void register(SmsBlend smsBlend) {
        if (smsBlend == null) {
            throw new SmsBlendException("短信服务对象不能为空");
        }
        BLENDS.put(smsBlend.getConfigId(), smsBlend);
        SmsLoad.starConfig(smsBlend, 1);
    }

    /**
     * 注册短信服务对象
     *
     * @param smsBlend 短信服务对象
     */
    public static void register(SmsBlend smsBlend, Integer weight) {
        if (smsBlend == null) {
            throw new SmsBlendException("短信服务对象不能为空");
        }
        BLENDS.put(smsBlend.getConfigId(), smsBlend);
        SmsLoad.starConfig(smsBlend, weight);
    }

    /**
     * 以configId为标识，当短信服务对象不存在时，进行注册
     *
     * @param smsBlend 短信服务对象
     * @return 是否注册成功
     * <p>当对象不存在时，进行注册并返回true</p>
     * <p>当对象已存在时，返回false</p>
     */
    public static boolean registerIfAbsent(SmsBlend smsBlend) {
        if (smsBlend == null) {
            throw new SmsBlendException("短信服务对象不能为空");
        }
        String configId = smsBlend.getConfigId();
        if (BLENDS.containsKey(configId)) {
            return false;
        }
        BLENDS.put(configId, smsBlend);
        SmsLoad.starConfig(smsBlend, 1);
        return true;
    }

    /**
     * registerIfAbsent
     * <p> 以configId为标识，当短信服务对象不存在时，进行注册。并添加至系统的负载均衡器
     *
     * @param smsBlend 短信服务对象
     * @param weight   权重
     * @return 是否注册成功
     * <p>当对象不存在时，进行注册并返回true</p>
     * <p>当对象已存在时，返回false</p>
     * @author :Wind
     */
    public static boolean registerIfAbsent(SmsBlend smsBlend, Integer weight) {
        if (smsBlend == null) {
            throw new SmsBlendException("短信服务对象不能为空");
        }
        String configId = smsBlend.getConfigId();
        if (BLENDS.containsKey(configId)) {
            return false;
        }
        BLENDS.put(configId, smsBlend);
        SmsLoad.starConfig(smsBlend, weight);
        return true;
    }

    /**
     * 注销短信服务对象
     * <p>与此同时会注销掉负载均衡器中已经存在的对象</p>
     *
     * @param configId 标识
     * @return 是否注销成功
     * <p>当configId存在时，进行注销并返回true</p>
     * <p>当configId不存在时，返回false</p>
     */
    public static boolean unregister(String configId) {
        SmsBlend blend = BLENDS.remove(configId);
        SmsLoad.getBeanLoad().removeLoadServer(blend);
        return blend != null;
    }

    /**
     * reload
     * <p> 重新读取并刷新缓存内短信实例
     *
     * @param configId      配置标识
     * @param smsReadConfig 配置接口实现对象
     * @author :Wind
     */
    public static void reload(String configId, SmsReadConfig smsReadConfig) {
        SmsFactory.unregister(configId);
        SmsFactory.createRestrictedSmsBlend(smsReadConfig, configId);
    }

    /**
     *  reloadAll
     * <p> 重新读取并刷新全部短信实例
     * @param smsReadConfig 配置接口实现对象
     * @author :Wind
    */
    public static void reloadAll(SmsReadConfig smsReadConfig) {
        List<BaseConfig> supplierConfigList = smsReadConfig.getSupplierConfigList();
        for (BaseConfig baseConfig : supplierConfigList) {
          reload(baseConfig.getConfigId(),smsReadConfig);
        }
    }

}
