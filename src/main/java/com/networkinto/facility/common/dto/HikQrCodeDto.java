package com.networkinto.facility.common.dto;

import lombok.Data;

/**
 * @author cuiEnMing
 * @date 2021/5/21 15:39
 */
@Data
public class HikQrCodeDto {
    /**
     * 设备序列号(必须)
     */
    private String sn;
    /**
     * 事件流水号（必须）
     */
    private String serialNo;
    /**
     * 认证方式：1-刷卡 ，2-刷脸，3-刷身份证，4-刷二维码
     */
    private String verifyType;
    /**
     * 进出方向：1-进  2-出
     */
    private String direction;
    /**
     * 二维码字符串。
     */
    private String qrCode;
    /**
     * 设备类型。
     */
    private String deviceType;
    /**
     * 提示文本。
     */
    private String message;
    /**
     * o成功 1失败。
     */
    private Integer code;
    /**
     * 失败原因
     */
    private String data;
}
