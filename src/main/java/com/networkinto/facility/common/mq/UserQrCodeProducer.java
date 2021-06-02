package com.networkinto.facility.common.mq;

import com.networkinto.facility.common.dto.CheckQrCode;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 用户qrCode
 *二维码
 * @author cuiEnMing
 * @date 2021/5/24 13:55
 */
@Log4j2
@Component
public class UserQrCodeProducer {
    /**
     * 引入mq模板
     */
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendMsg(CheckQrCode check) {
        rocketMQTemplate.convertAndSend("USER_QR_CODE_CHECK", check);
    }
}
