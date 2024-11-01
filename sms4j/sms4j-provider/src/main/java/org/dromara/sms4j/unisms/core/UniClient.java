package org.dromara.sms4j.unisms.core;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.comm.constant.Constant;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.dromara.sms4j.comm.utils.SmsHttpUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

@Slf4j
public class UniClient {

    public static final String USER_AGENT = "uni-java-sdk" + "/" + Uni.VERSION;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String endpoint;
    private final String signingAlgorithm;
    private boolean isSimple;
    private final int retryInterval;
    private final int maxRetries;
    private int retry = 0;
    private final SmsHttpUtils http = SmsHttpUtils.instance();

    protected UniClient(Builder b) {
        this.accessKeyId = b.accessKeyId;
        this.accessKeySecret = b.accessKeySecret;
        this.endpoint = b.endpoint;
        this.signingAlgorithm = b.signingAlgorithm;
        this.retryInterval = b.retryInterval;
        this.maxRetries = b.maxRetries;
    }

    private static String getSignature(final String message, final String secretKey) {
        try {
            HMac hMac = new HMac(HmacAlgorithm.HmacSHA256, secretKey.getBytes());
            byte[] bytes = hMac.digest(message.getBytes());
            return Base64.encode(bytes);
        } catch (Exception e) {
            return null;
        }
    }

    private static String queryStringify(final Map<String, Object> params) {
        Map<String, Object> sortedMap = new TreeMap<>(new MapKeyComparator());
        sortedMap.putAll(params);
        StringBuilder sb = new StringBuilder();

        for (Entry<String, Object> stringObjectEntry : sortedMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(((Entry<?, ?>) stringObjectEntry).getKey()).append("=").append(((Entry<?, ?>) stringObjectEntry).getValue());
        }

        return sb.toString();
    }

    private Map<String, Object> sign(final Map<String, Object> query) {
        if (this.accessKeySecret != null) {
            query.put("algorithm", this.signingAlgorithm);
            query.put("timestamp", System.currentTimeMillis());
            query.put("nonce", UUID.randomUUID().toString().replaceAll("-", ""));

            String strToSign = UniClient.queryStringify(query);
            query.put("signature", UniClient.getSignature(strToSign, this.accessKeySecret));
        }
        return query;
    }

    /**
     * request
     * <p>向 uni-sms发送请求
     *
     * @param action 接口名称
     * @author :Wind
     */
    public UniResponse request(final String action, final Map<String, Object> data) throws SmsBlendException {
        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", USER_AGENT);
        headers.put(Constant.CONTENT_TYPE, Constant.APPLICATION_JSON_UTF8);
        headers.put(Constant.ACCEPT, Constant.APPLICATION_JSON);
        String url;
        if (this.isSimple) {
            url = this.endpoint + "?action=" + action + "&accessKeyId=" + this.accessKeyId;
        } else {
            Map<String, Object> d = new HashMap<>();
            Map<String, Object> signed = sign(d);
            url = this.endpoint + "?action=" + action + "&accessKeyId=" + this.accessKeyId + "&algorithm=" + signed.get("algorithm") +
                    "&timestamp=" + signed.get("timestamp") + "&nonce=" + signed.get("nonce") + "&signature=" + signed.get("signature");
        }
        UniResponse smsResponse;
        try {
            smsResponse = new UniResponse(http.postJson(url, headers, data));
        } catch (SmsBlendException e) {
            smsResponse = new UniResponse();
            smsResponse.message = "Error";
        }
        if ("Success".equals(smsResponse.message) || retry == maxRetries) {
            retry = 0;
            return smsResponse;
        }
        return requestRetry(action, data);
    }

    private UniResponse requestRetry(String action, Map<String, Object> data) {
        http.safeSleep(retryInterval);
        retry++;
        log.warn("短信第 {} 次重新发送", retry);
        return request(action, data);
    }

    public static class Builder {
        private final String accessKeyId;
        private String accessKeySecret;
        private String endpoint;
        private String signingAlgorithm;
        private int retryInterval;
        private int maxRetries;
        private boolean isSimple;

        public Builder isSimple(boolean isSimple) {
            this.isSimple = isSimple;
            return this;
        }

        public Builder(final String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public Builder(final String accessKeyId, final String accessKeySecret) {
            this.accessKeyId = accessKeyId;
            this.accessKeySecret = accessKeySecret;
        }

        public Builder accessKeySecret(final String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
            return this;
        }

        public Builder endpoint(final String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder signingAlgorithm(final String signingAlgorithm) {
            this.signingAlgorithm = signingAlgorithm;
            return this;
        }

        public Builder setRetryInterval(int retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        public Builder setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public UniClient build() {
            return new UniClient(this);
        }
    }
}

class MapKeyComparator implements Comparator<String> {
    @Override
    public int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }
}
