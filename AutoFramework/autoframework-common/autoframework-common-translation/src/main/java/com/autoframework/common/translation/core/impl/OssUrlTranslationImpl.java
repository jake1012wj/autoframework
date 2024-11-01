package com.autoframework.common.translation.core.impl;

import com.autoframework.common.translation.annotation.TranslationType;
import com.autoframework.common.translation.constant.TransConstant;
import com.autoframework.common.translation.core.TranslationInterface;
import com.autoframework.resource.api.RemoteFileService;
import lombok.AllArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;

/**
 * OSS翻译实现
 *
 * @author Lion Li
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.OSS_ID_TO_URL)
public class OssUrlTranslationImpl implements TranslationInterface<String> {

    @DubboReference(mock = "true")
    private RemoteFileService remoteFileService;

    @Override
    public String translation(Object key, String other) {
        return remoteFileService.selectUrlByIds(key.toString());
    }
}
