package com.autoframework.resource.dubbo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import com.autoframework.common.core.exception.ServiceException;
import com.autoframework.common.mail.utils.MailUtils;
import com.autoframework.resource.api.RemoteMailService;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
@DubboService
public class RemoteMailServiceImpl implements RemoteMailService {

    /**
     * 发送邮件
     *
     * @param to      接收人
     * @param subject 标题
     * @param text    内容
     */
    @Override
    public void send(String to, String subject, String text) throws ServiceException {
        MailUtils.sendText(to, subject, text);
    }

}
