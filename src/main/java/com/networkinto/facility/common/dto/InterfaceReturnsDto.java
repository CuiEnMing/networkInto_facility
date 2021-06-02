package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cuiEnMing
 * @Desc 外部接口
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceReturnsDto {
    /**
     * 0成功
     */
    private Integer code;
    private String message;
    private String data;
}
