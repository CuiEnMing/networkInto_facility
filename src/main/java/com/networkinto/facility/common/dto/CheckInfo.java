package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cuiEnMing
 * @date 2021/5/31 16:41
 */@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInfo {
    /**
     * 欢迎光临
     */
    private String prompts;
    private CheckInfo1 userInfo= new CheckInfo1();
}
