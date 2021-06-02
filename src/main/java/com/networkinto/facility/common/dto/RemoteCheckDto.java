package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cuiEnMing
 * @date 2021/5/21 16:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteCheckDto {
    /**
     *远程核验接口
     */
    private String remoteAuthAddress;
    /**
     *是否使能远程核验，0-关闭；1-人证比对开启远程核验；2-刷脸开启远程核验；3-人证、刷脸开启远程核验；4-刷卡开启远程核验；5-人证、
     * 刷卡开启远程核验；6-刷脸、刷卡开启远程核验；7-人证、刷脸、刷卡均开启远程核验，默认全部开启
     */
    private int enableRemote;
    /**
     *设备进出方向，1-进，2-出
     */
    private int devDirection;
    /**
     *与平台通信的超时时间，范围5-20s，默认5s
     */
    private int connectTime;
    /**
     *自定义提示信息  若保持为空则设备本地核验提示内容与基线一致
     */
    private LocalAuthPromptsDto localAuthPrompts= new LocalAuthPromptsDto();
}
