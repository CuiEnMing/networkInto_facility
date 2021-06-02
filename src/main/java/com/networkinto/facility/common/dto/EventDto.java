package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 事件记录对象 eventdata
 *
 * @author admin
 * createTime 2021-02-03
 * lastModify admin
 * lastModifyTime 2021-02-03
 * group 电子政务小组
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
    /**
     * 用户名
     */
    private String name;

    /**
     * 事件时间
     */
    private String eventtime;
    /**
     * IP地址
     */
    private String ip;
    /**
     * 验证方式 0 人脸 1 二维码
     */
    private int checkType;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 序列号
     */
    private String sn;
    /**
     * 身份证
     */
    private String identity;

    /**
     * 地址
     */
    private String address;
    /**
     * 地址
     */
    private String imgUrl;
}
