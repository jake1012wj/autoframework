package org.dromara.sms4j.emay.service;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.api.utils.SmsRespUtils;
import org.dromara.sms4j.comm.constant.Constant;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import org.dromara.sms4j.comm.delayedTime.DelayedTime;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.dromara.sms4j.comm.utils.SmsUtils;
import org.dromara.sms4j.emay.config.EmayConfig;
import org.dromara.sms4j.emay.util.EmayBuilder;
import org.dromara.sms4j.provider.service.AbstractSmsBlend;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * @author Richard
 * @date 2023-04-11 12:00
 */
@Slf4j
public class EmaySmsImpl extends AbstractSmsBlend<EmayConfig> {

    private int retry = 0;

    public EmaySmsImpl(EmayConfig config, Executor pool, DelayedTime delayed) {
        super(config, pool, delayed);
    }

    public EmaySmsImpl(EmayConfig config) {
        super(config);
    }

    @Override
    public String getSupplier() {
        return SupplierConstant.EMAY;
    }

    @Override
    public SmsResponse sendMessage(String phone, String message) {
        EmayConfig config = getConfig();
        String url = config.getRequestUrl();
        Map<String, Object> params = EmayBuilder.buildRequestBody(config.getAccessKeyId(), config.getAccessKeySecret(), phone, message);

        Map<String, String> headers = MapUtil.newHashMap(1, true);
        headers.put(Constant.CONTENT_TYPE, Constant.APPLICATION_FROM_URLENCODED);
        SmsResponse smsResponse;
        try {
            smsResponse = getResponse(http.postUrl(url, headers, params));
        } catch (SmsBlendException e) {
            smsResponse = errorResp(e.message);
        }
        if (smsResponse.isSuccess() || retry == config.getMaxRetries()) {
            retry = 0;
            return smsResponse;
        }
        return requestRetry(phone, message);
    }

    @Override
    public SmsResponse sendMessage(String phone, LinkedHashMap<String, String> messages) {
        if (Objects.isNull(messages)){
            messages = new LinkedHashMap<>();
        }
        return sendMessage(phone, getConfig().getTemplateId(), messages);
    }

    private SmsResponse requestRetry(String phone, String message) {
        http.safeSleep(getConfig().getRetryInterval());
        retry++;
        log.warn("短信第 {} 次重新发送", retry);
        return sendMessage(phone, message);
    }


    @Override
    public SmsResponse sendMessage(String phone, String templateId, LinkedHashMap<String, String> messages) {
        if (Objects.isNull(messages)){
            messages = new LinkedHashMap<>();
        }
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            list.add(entry.getValue());
        }
        return sendMessage(phone, EmayBuilder.listToString(list));
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String message) {
        if (phones.size() > 500) {
            throw new SmsBlendException("单次发送超过最大发送上限，建议每次群发短信人数低于500");
        }
        return sendMessage(SmsUtils.joinComma(phones), message);
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String templateId, LinkedHashMap<String, String> messages) {
        if (phones.size() > 500) {
            throw new SmsBlendException("单次发送超过最大发送上限，建议每次群发短信人数低于500");
        }
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : messages.entrySet()) {
            list.add(entry.getValue());
        }
        return sendMessage(SmsUtils.joinComma(phones), EmayBuilder.listToString(list));
    }

    private SmsResponse getResponse(JSONObject resJson) {
        return SmsRespUtils.resp(resJson, "success".equalsIgnoreCase(resJson.getStr("code")), getConfigId());
    }

}
