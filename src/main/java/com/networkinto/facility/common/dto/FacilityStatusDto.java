package com.networkinto.facility.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * 设备状态
 *
 * @author cuiEnMing
 * @date 2021/5/18 9:09
 */
@Data
@ApiModel("设备状态dto")
public class FacilityStatusDto {
    @ApiModelProperty("账号")
    @NotBlank(message = "设备账号不允许为空")
    private String account;
    @ApiModelProperty("密码")
    @NotBlank(message = "密码不允许为空")
    private String password;
    @ApiModelProperty("IP")
    @NotBlank(message = "ip不允许为空")
    private String ip;
    @ApiModelProperty("端口")
    @Min(0)
    private int port;
    @ApiModelProperty("设备状态 0_正常 ，1_离线，2_设备注册异常")
    @Digits(integer = 0, fraction = 2)
    private int status;
    @ApiModelProperty("序列号")
    @NotBlank(message = "序列号不允许为空")
    private String serialNumber;
    @ApiModelProperty("设备类型 0_大华 ，1_海康")
    @Digits(integer = 0, fraction = 1)
    private int type;

}
