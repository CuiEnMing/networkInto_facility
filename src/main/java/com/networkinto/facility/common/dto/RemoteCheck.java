package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cuiEnMing
 * @date 2021/5/26 14:53
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteCheck {
    /**
     * 事件流水号
     */
    private Integer serialNo;
    /**
     * 验证结果
     */
    private String checkResult;
    //   private CheckInfo info = new CheckInfo();

}
