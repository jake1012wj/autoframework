package org.dromara.oa.core.provider.factory;

import org.dromara.oa.api.OaSender;
import org.dromara.oa.comm.config.OaSupplierConfig;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class OaAbstractProviderFactory<S extends OaSender, C extends OaSupplierConfig> implements OaBaseProviderFactory<S, C> {

    private Class<C> configClass;

    public OaAbstractProviderFactory() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericSuperclass;
            Type[] typeArguments = paramType.getActualTypeArguments();
            if (typeArguments.length > 1 && typeArguments[1] instanceof Class) {
                configClass = (Class<C>) typeArguments[1];
            }
        }
    }

    /**
     * 获取配置类
     *
     * @return 配置类
     */
    @Override
    public Class<C> getConfigClass() {
        return configClass;
    }

}
