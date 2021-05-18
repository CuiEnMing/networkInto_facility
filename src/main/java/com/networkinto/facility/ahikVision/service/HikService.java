package com.networkinto.facility.ahikVision.service;

import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.HumanFaceDto;

import java.util.List;

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

    /**
     * 设备卡查询
     *
     * @param facilityDto
     * @return HumanFaceDto
     */
    List<CardDataDto> queryCard(FacilityDto facilityDto);

    /**
     * 设备卡删除
     *
     * @param cardDataDto
     * @return s
     */
    String deleteCard(CardDataDto cardDataDto);
}
