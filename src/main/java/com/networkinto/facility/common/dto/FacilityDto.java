package com.networkinto.facility.common.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cuiEnMing
 * @Desc 设备信息对接格式
 */
@Data
public class FacilityDto implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 设备ID
     */
    private String id;
    /**
     * 用户
     */
    private String account;
    /**
     * 密码
     */
    private String password;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private int port;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 人脸路径
     */
    private String imgUrl;
    /**
     * 设备类型 2 大华 1 海康
     */
    private int deviceType;
}
