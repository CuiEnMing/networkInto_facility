package com.networkinto.facility.common.dto;

import lombok.Data;

/**
 * @author cuiEnMing
 * @Desc
 * @data 2021/4/27 17:56
 */
@Data
public class CardDataDto {
    /**
     * 卡号
     */
    private int cardNo;
    /**
     * 序号
     */
    private int recNo;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 设备序列号
     */
    private String serialNumber;
    /**
     * 图片路径
     */
    private String imgUrl;
}
