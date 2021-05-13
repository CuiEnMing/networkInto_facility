package com.networkinto.facility.common.dto;

import lombok.Data;

/**
 * @author cuiEnMing
 * @Desc 人脸回调信息
 */
@Data
public class FaceInfoDto {
    /**
     * 用户ID
     */
    public String userId;
    /**
     * 卡号
     */
    public String cardNo;
    /**
     * 事件发生时间
     */
    public String eventTime;
    /**
     * 开门方式
     */
    public int openDoorMethod;
    /**
     * 开门结果 1表示成功, 0表示失败
     */
    public int bStatus;
    /**
     * 卡名
     */
    public String szCardName;
}
