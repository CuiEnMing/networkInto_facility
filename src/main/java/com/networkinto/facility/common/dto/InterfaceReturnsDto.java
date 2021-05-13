package com.networkinto.facility.common.dto;

import lombok.Data;

/**
 * @author cuiEnMing
 * @Desc 外部接口
 */
@Data
public class InterfaceReturnsDto {
    private String c;
    private String d;
    private String m;
    /**
     * 0成功
     */
    private Integer code;
    private String message;
    private String data;
}
