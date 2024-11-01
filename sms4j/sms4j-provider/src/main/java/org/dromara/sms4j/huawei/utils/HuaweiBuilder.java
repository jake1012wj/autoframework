package org.dromara.sms4j.huawei.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.comm.constant.Constant;
import org.dromara.sms4j.comm.exception.SmsBlendException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HuaweiBuilder {
    private HuaweiBuilder() {
    }

    /**
     * buildWsseHeader
     * <p>构造X-WSSE参数值
     *
     * @author :Wind
     */
    public static String buildWsseHeader(String appKey, String appSecret) {
        if (null == appKey || null == appSecret || appKey.isEmpty() || appSecret.isEmpty()) {
            log.error("buildWsseHeader(): appKey or appSecret is null.");
            throw new SmsBlendException("buildWsseHeader(): appKey or appSecret is null.");
        }
        String time = dateFormat(new Date());
        // Nonce
        String nonce = UUID.fastUUID().toString(true);
        byte[] passwordDigest = DigestUtil.sha256(nonce + time + appSecret);
        // PasswordDigest
        String passwordDigestBase64Str = Base64.encode(passwordDigest);
        //若passwordDigestBase64Str中包含换行符,请执行如下代码进行修正
        //passwordDigestBase64Str = passwordDigestBase64Str.replaceAll("[\\s*\t\n\r]", "");
        return String.format(Constant.HUAWEI_WSSE_HEADER_FORMAT, appKey, passwordDigestBase64Str, nonce, time);
    }

    static void trustAllHttpsCertificates() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * buildRequestBody
     * <p>构造请求Body体
     *
     * @param sender         国内短信签名通道号
     * @param receiver       短信接收者
     * @param templateId     短信模板id
     * @param templateParas  模板参数
     * @param statusCallBack 短信状态报告接收地
     * @param signature      | 签名名称,使用国内短信通用模板时填写
     * @author :Wind
     */
    public static String buildRequestBody(String sender, String receiver, String templateId, String templateParas,
                                          String statusCallBack, String signature) {
        if (StrUtil.hasBlank(sender, receiver, templateId)) {
            log.error("buildRequestBody(): sender, receiver or templateId is null.");
            throw new SmsBlendException("buildRequestBody(): sender, receiver or templateId is null.");
        }
        Map<String, String> map = new HashMap<>(3);

        map.put("from", sender);
        map.put("to", receiver);
        map.put("templateId", templateId);
        if (null != templateParas && !templateParas.isEmpty()) {
            map.put("templateParas", templateParas);
        }
        if (null != statusCallBack && !statusCallBack.isEmpty()) {
            map.put("statusCallback", statusCallBack);
        }
        if (null != signature && !signature.isEmpty()) {
            map.put("signature", signature);
        }

        StringBuilder sb = new StringBuilder();
        map.keySet().forEach(s -> sb.append(s).append("=").append(URLEncodeUtil.encode(map.get(s))).append("&"));
        return sb.deleteCharAt(sb.length() - 1).toString();
    }

    public static String listToString(List<String> list) {
        if (null == list || list.isEmpty()) {
            return null;
        }
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("[\"");
        for (String s : list) {
            stringBuffer.append(s);
            stringBuffer.append("\"");
            stringBuffer.append(",");
            stringBuffer.append("\"");
        }
        stringBuffer.delete(stringBuffer.length() - 3, stringBuffer.length() - 1);
        stringBuffer.append("]");
        return stringBuffer.toString();
    }

    private static String dateFormat(Date date) {
        return DateUtil.format(date, Constant.HUAWEI_JAVA_DATE);
    }

}
