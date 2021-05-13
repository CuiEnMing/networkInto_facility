package com.networkinto.facility.ahikVision.service;

import com.networkinto.facility.common.dto.HumanFaceDto;

/**
 * 海康设备管理
 *
 * @author cuiEnMing
 * @date 2021/5/12 10:16
 */
public interface HikService {
    /**
     * 卡下发
     *
     * @param faceDto
     * @return HumanFaceDto
     */
    HumanFaceDto addCard(HumanFaceDto faceDto);

    /**
     * 人脸下发
     *
     * @param bytes
     * @param faceDto
     * @return HumanFaceDto
     */
    HumanFaceDto addPicture(byte[] bytes, HumanFaceDto faceDto);
}
