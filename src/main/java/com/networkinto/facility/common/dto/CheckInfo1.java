package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cuiEnMing
 * @date 2021/5/31 16:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckInfo1 {
    /**
     * 编码格式
     */
    private String face_picurl;
    /**
     * 欢迎光临
     */
    private String face_picdata;
    private Integer faceDataType;
    /**
     * 欢迎光临
     */
    private Integer cardtype;
    private String cardNum;
    private String name;
    private Integer gender;


}
