package com.networkinto.facility.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cuiEnMing
 * @Desc 人脸下发dto
 */
@Data
public class HumanFaceDto {
    private static final long serialVersionUID = 1L;
    /**
     * 任务id
     */
    private Long taskId;
    /**
     * 人脸url
     */
    private String faceUrl;
    /**
     * 用户姓名
     */
    private String personName;
    /**
     * 设备ID
     */
    private Long deviceId;
    /**
     * 设备IP地址
     */
    private String ip;
    /**
     * 序列号
     */
    private String serialNumber;
    /**
     * 设备类型 0大华 1海康
     */
    private Long deviceType;
    /**
     * 人脸下发状态:1=成功,0=未下发,-1=下发失败,2=删除成功,-2=删除失败
     */
    private Integer dfaceStatus;
    /**
     * 卡号ID
     */
    private Long cardNo;
    /**
     * 卡片下发状态:1=成功,0=未下发,-1=下发失败,2=删除成功,-2=删除失败
     */
    private Integer cardStatus;
    /**
     * 指纹下发状态:1=成功,0=未下发,-1=下发失败,2=删除成功,-2=删除失败
     */
    private Integer fingerStatus;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 过期时间，0代表无限期
     */
    private LocalDateTime expiresTime;

    /**
     * 失败描述
     */
    private String failRemark;

    /**
     * 下发失败次数
     */
    private Long noNum;
}
