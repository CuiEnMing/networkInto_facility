package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 与智慧小区验证二维码权限
 *
 * @author cuiEnMing
 * @date 2021/5/31 10:03
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckQrCode {
    /**
     * 二维码
     */
    private String QrCode;
    /**
     * 设备序列号
     */
    private String serial;
}
