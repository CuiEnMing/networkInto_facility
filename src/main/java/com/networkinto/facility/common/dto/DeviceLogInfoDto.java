package com.networkinto.facility.common.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * @author cuiEnMing
 * @Desc 设备关键日志存入数据库 数据传输dto
 */
@Data
public class DeviceLogInfoDto {
    /**
     * 设备ip
     */
    private String ip;
    /**
     * 日志类型 0 正常日志  1 异常日志
     */
    private String type;
    /**
     * 日志详情
     */
    private String info;
    /**
     * 日志产生时间
     */
    private LocalDate date;
}
