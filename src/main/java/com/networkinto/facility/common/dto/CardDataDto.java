package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询卡返回实体
 *
 * @author cuiEnMing
 * @Desc
 * @data 2021/4/27 17:56
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDataDto {
    /**
     * 卡号
     */
    private String cardNo;
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
