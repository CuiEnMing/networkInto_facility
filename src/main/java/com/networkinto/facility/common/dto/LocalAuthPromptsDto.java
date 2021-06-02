package com.networkinto.facility.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cuiEnMing
 * @date 2021/5/21 16:19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalAuthPromptsDto {
    /**
     * 人证比对本地核验成功提示信息
     */
    private String idAuthSucc;
    /**
     * 人证比对本地核验失败提示信息
     */
    private String idAuthFail;
    /**
     * 刷脸比对本地核验成功提示信息
     */
    private String faceAuthSucc;
    /**
     * 刷脸比对本地核验失败提示信息
     */
    private String faceAuthFail;
    /**
     * 刷卡比对本地核验成功提示信息
     */
    private String cardAuthSucc;
    /**
     * 刷卡比对本地核验失败提示信息
     */
    private String cardAuthFail;
}
