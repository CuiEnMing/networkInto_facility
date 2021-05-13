package com.networkinto.facility.ajHua.service;

import com.networkinto.facility.ajHua.utils.JsonResult;
import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.HumanFaceDto;
import com.networkinto.facility.common.dto.InterfaceReturnsDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @author cuiEnMing
 * @Desc 门禁设备用户管理 （卡号 ，图片）
 */
public interface AjHuaService {
    /**
     * add Face
     *
     * @param file
     * @param device
     * @return
     */
    List<Map<String, String>> addFace(List<MultipartFile> file, List<HumanFaceDto> device);

    /**
     * 验证二维码权限
     *
     * @param qrCode 二维码
     * @return
     */
    JsonResult<String> checkQrCode(String qrCode);

    /**
     * 关闭二维码穿透
     *
     * @param facilityDto 设备
     * @return
     */
    InterfaceReturnsDto closeQrCOde(FacilityDto facilityDto);

    /**
     * 查询信息
     * * @return
     *
     * @param serialNumber
     */
    List<CardDataDto> queryCard(String serialNumber);

    /**
     * 设备登出
     *
     * @param deviceDto
     * @return JsonResult<String>
     */
    JsonResult<String> outDevice(String deviceDto);

    /**
     * 人脸下发
     *
     * @param fileBytes
     * @param fileName
     * @param dto
     * @return boolean
     */
    HumanFaceDto addPicture(byte[] fileBytes, String fileName, HumanFaceDto dto);

    /**
     * 卡信息修改
     *
     * @param cardDataDto
     * @return boolean
     */
    JsonResult<String> updateCard(CardDataDto cardDataDto);

    /**
     * 卡信息删除
     *
     * @param cardDataDto
     * @return boolean
     */
    JsonResult<String> deleteCard(CardDataDto cardDataDto);

}
