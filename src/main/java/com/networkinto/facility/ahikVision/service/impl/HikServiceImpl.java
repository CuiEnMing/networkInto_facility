package com.networkinto.facility.ahikVision.service.impl;

import com.networkinto.facility.ahikVision.module.HikVisionModule;
import com.networkinto.facility.ahikVision.service.HikService;
import com.networkinto.facility.ahikVision.utils.HCNetSDK;
import com.networkinto.facility.common.constant.JsonResult;
import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.HumanFaceDto;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cuiEnMing
 * @date 2021/5/12 10:18
 */
@Log4j2
@Service("HikService")
public class HikServiceImpl implements HikService {
    @Resource
    private HikVisionModule hikVisionModule;

    /**
     * 人脸下发
     *
     * @param faceDto
     * @return HumanFaceDto
     */
    @Override
    public HumanFaceDto addCard(HumanFaceDto faceDto) {
        byte card_type = 1;
        String[] doorRights = {"0"};
        String cardId = String.valueOf(faceDto.getCardNo());
        HCNetSDK.NET_DVR_CARD_COND struCardCond = new HCNetSDK.NET_DVR_CARD_COND();
        struCardCond.read();
        struCardCond.dwSize = struCardCond.size();
        //下发一张
        struCardCond.dwCardNum = 1;
        struCardCond.write();
        Pointer ptrStruCond = struCardCond.getPointer();
        hikVisionModule.cardHandle = hikVisionModule.hCNetSDK.NET_DVR_StartRemoteConfig(hikVisionModule.getUserHandleMap()
                        .get(faceDto.getSerialNumber()), HCNetSDK.NET_DVR_SET_CARD,
                ptrStruCond, struCardCond.size(), null, null);
        if (hikVisionModule.cardHandle == -1) {
            log.error("建立下发卡长连接失败，错误码为" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            faceDto.setNoNum(faceDto.getNoNum() + 1);
            faceDto.setDfaceStatus(-1);
            if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            } else {
                faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            }
            return faceDto;
        } else {
            log.info("建立下发卡长连接成功！");
        }
        HCNetSDK.NET_DVR_CARD_RECORD struCardRecord = new HCNetSDK.NET_DVR_CARD_RECORD();
        struCardRecord.read();
        struCardRecord.dwSize = struCardRecord.size();
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            struCardRecord.byCardNo[i] = 0;
        }
        for (int i = 0; i < cardId.length(); i++) {
            struCardRecord.byCardNo[i] = cardId.getBytes()[i];
        }
        //普通卡
        struCardRecord.byCardType = card_type;
        //是否为首卡，0-否，1-是
        struCardRecord.byLeaderCard = 0;
        struCardRecord.byUserType = 0;
        if (doorRights == null || doorRights.length == 0) {
            //门1有权限
            struCardRecord.byDoorRight[0] = 1;
        } else {
            for (int i = 0; i < doorRights.length; i++) {
                struCardRecord.byDoorRight[Integer.parseInt(doorRights[i])] = 1;
            }
        }
        HCNetSDK.NET_DVR_TIME_EX startTime = new HCNetSDK.NET_DVR_TIME_EX((short) faceDto.getStartTime().getYear(),
                (byte) faceDto.getStartTime().get(ChronoField.MONTH_OF_YEAR), (byte) faceDto.getStartTime().getDayOfMonth(),
                (byte) faceDto.getStartTime().getHour(), (byte) faceDto.getStartTime().getMinute(), (byte) faceDto.getStartTime().getSecond(), (byte) 0);
        HCNetSDK.NET_DVR_TIME_EX endTime = new HCNetSDK.NET_DVR_TIME_EX((short) faceDto.getExpiresTime().getYear(),
                (byte) faceDto.getExpiresTime().get(ChronoField.MONTH_OF_YEAR), (byte) faceDto.getExpiresTime().getDayOfMonth(),
                (byte) faceDto.getExpiresTime().getHour(), (byte) faceDto.getExpiresTime().getMinute(), (byte) faceDto.getExpiresTime().getSecond(), (byte) 0);
        //卡有效期
        struCardRecord.struValid.byEnable = 1;
        struCardRecord.struValid.struBeginTime = startTime;
        struCardRecord.struValid.struEndTime = endTime;
        //卡计划模板1有效
        struCardRecord.wCardRightPlan[0] = 1;
        String time = String.valueOf(Instant.now().toEpochMilli());
        //工号
        struCardRecord.dwEmployeeNo = Integer.parseInt(time.substring(time.length() - 5));
        //姓名
        byte[] strCardName = new byte[0];
        try {
            strCardName = faceDto.getPersonName().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < HCNetSDK.NAME_LEN; i++) {
            struCardRecord.byName[i] = 0;
        }
        for (int i = 0; i < strCardName.length; i++) {
            struCardRecord.byName[i] = strCardName[i];
        }
        struCardRecord.write();
        HCNetSDK.NET_DVR_CARD_STATUS struCardStatus = new HCNetSDK.NET_DVR_CARD_STATUS();
        struCardStatus.read();
        struCardStatus.dwSize = struCardStatus.size();
        struCardStatus.write();
        IntByReference pInt = new IntByReference(0);
        while (true) {
            hikVisionModule.cardState = hikVisionModule.hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(hikVisionModule.cardHandle,
                    struCardRecord.getPointer(), struCardRecord.size(), struCardStatus.getPointer(), struCardStatus.size(), pInt);
            struCardStatus.read();
            if (hikVisionModule.cardState == -1) {
                log.error("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                log.error("下发卡失败, 卡号: " + new String(struCardStatus.byCardNo).trim() + ", 错误码：" + struCardStatus.dwErrorCode);
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                log.error("下发卡异常, 卡号: " + new String(struCardStatus.byCardNo).trim() + ", 错误码：" + struCardStatus.dwErrorCode);
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                if (struCardStatus.dwErrorCode != 0) {
                    log.error("下发卡成功,但是错误码" + struCardStatus.dwErrorCode + ", 卡号：" + new String(struCardStatus.byCardNo).trim());
                    faceDto.setNoNum(faceDto.getNoNum() + 1);
                    faceDto.setDfaceStatus(-1);
                    if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                        faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                    } else {
                        faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                    }
                    return faceDto;
                } else {
                    log.info("下发卡成功, 卡号: " + new String(struCardStatus.byCardNo).trim() + ", 状态：" + struCardStatus.byStatus);
                }
                IntByReference intByReference = new IntByReference(struCardStatus.dwErrorCode);
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                log.info("下发卡完成 , 卡号: " + new String(struCardStatus.byCardNo).trim());
                break;
            }
        }
        IntByReference intByReference = new IntByReference();
        intByReference.setValue(struCardStatus.dwErrorCode);
        if (!hikVisionModule.hCNetSDK.NET_DVR_StopRemoteConfig(hikVisionModule.cardHandle)) {
            log.error("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            faceDto.setCardStatus(-1);
            faceDto.setNoNum(faceDto.getNoNum() + 1);
            faceDto.setDfaceStatus(-1);
            if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            } else {
                faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            }
            return faceDto;
        }
        if (!hikVisionModule.hCNetSDK.NET_DVR_StopRemoteConfig(hikVisionModule.faceHandle)) {
            log.error("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
        }
        faceDto.setCardStatus(1);
        return faceDto;
    }

    /**
     * 人脸下发
     *
     * @param faceDto
     * @return HumanFaceDto
     */
    @Override
    public HumanFaceDto addPicture(byte[] bytes, HumanFaceDto faceDto) {
        HCNetSDK.NET_DVR_FACE_COND struFaceCond = new HCNetSDK.NET_DVR_FACE_COND();
        struFaceCond.read();
        String cardNo = String.valueOf(faceDto.getCardNo());
        struFaceCond.dwSize = struFaceCond.size();
        struFaceCond.byCardNo = cardNo.getBytes();
        //下发一张
        struFaceCond.dwFaceNum = 1;
        //人脸读卡器编号
        struFaceCond.dwEnableReaderNo = 1;
        struFaceCond.write();
        Pointer ptrStruFaceCond = struFaceCond.getPointer();
        hikVisionModule.faceHandle = hikVisionModule.hCNetSDK.NET_DVR_StartRemoteConfig(hikVisionModule.getUserHandleMap().get(faceDto.getSerialNumber()),
                HCNetSDK.NET_DVR_SET_FACE, ptrStruFaceCond, struFaceCond.size(), null, null);
        if (hikVisionModule.faceHandle == -1) {
            log.error("建立下发人脸长连接失败，错误码为" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            faceDto.setNoNum(faceDto.getNoNum() + 1);
            faceDto.setDfaceStatus(-1);
            if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            } else {
                faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            }
            return faceDto;
        } else {
            log.info("建立下发人脸长连接成功！");
        }
        HCNetSDK.NET_DVR_FACE_RECORD struFaceRecord = new HCNetSDK.NET_DVR_FACE_RECORD();
        struFaceRecord.read();
        struFaceRecord.dwSize = struFaceRecord.size();
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            struFaceRecord.byCardNo[i] = 0;
        }
        for (int i = 0; i < cardNo.length(); i++) {
            struFaceRecord.byCardNo[i] = cardNo.getBytes()[i];
        }
        HCNetSDK.BYTE_ARRAY ptrpicByte = new HCNetSDK.BYTE_ARRAY(bytes);
        ptrpicByte.write();
        struFaceRecord.dwFaceLen = bytes.length;
        struFaceRecord.pFaceBuffer = ptrpicByte.getPointer();
        struFaceRecord.write();
        HCNetSDK.NET_DVR_FACE_STATUS struFaceStatus = new HCNetSDK.NET_DVR_FACE_STATUS();
        struFaceStatus.read();
        struFaceStatus.dwSize = struFaceStatus.size();
        struFaceStatus.write();
        IntByReference pInt = new IntByReference(0);
        while (true) {
            hikVisionModule.faceState = hikVisionModule.hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(hikVisionModule.faceHandle, struFaceRecord.getPointer(),
                    struFaceRecord.size(), struFaceStatus.getPointer(), struFaceStatus.size(), pInt);
            struFaceStatus.read();
            if (hikVisionModule.faceState == -1) {
                log.error("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.faceState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else if (hikVisionModule.faceState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                log.error("下发人脸失败, 卡号: " + new String(struFaceStatus.byCardNo).trim() + ", 错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.faceState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                log.error("下发卡异常, 卡号: " + new String(struFaceStatus.byCardNo).trim() + ", 错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.faceState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                if (struFaceStatus.byRecvStatus != 1) {
                    log.error("下发卡失败，人脸读卡器状态" + struFaceStatus.byRecvStatus + ", 卡号：" + new String(struFaceStatus.byCardNo).trim());
                    log.info("0-失败，1-成功，2-重试或人脸质量差，3-内存已满，4-已存在该人脸，5-非法人脸ID，\n" +
                            "6-算法建模失败，7-未下发卡权限，8-未定义（保留）\n，" +
                            "9-人眼间距小，10-图片数据长度小于1KB，11-图片格式不符（png/jpg/bmp）,12-图片像素数量超过上限，\n" +
                            "13-图片像素数量低于下限，14-图片信息校验失败，15-图片解码失败，16-人脸检测失败，17-人脸评分失败 \n");
                    faceDto.setNoNum(faceDto.getNoNum() + 1);
                    faceDto.setDfaceStatus(-1);
                    if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                        faceDto.setFailRemark(faceDto.getFailRemark() + ",读卡器状态" + struFaceStatus.byRecvStatus);
                    } else {
                        faceDto.setFailRemark("读卡器状态" + struFaceStatus.byRecvStatus);
                    }
                    return faceDto;
                } else {
                    log.info("下发卡成功, 卡号: " + new String(struFaceStatus.byCardNo).trim() + ", 状态：" + struFaceStatus.byRecvStatus);
                }
                IntByReference intByReference = new IntByReference(struFaceStatus.byRecvStatus);
                faceDto.setNoNum(faceDto.getNoNum() + 1);
                faceDto.setDfaceStatus(-1);
                if (StringUtil.isNotBlank(faceDto.getFailRemark())) {
                    faceDto.setFailRemark(faceDto.getFailRemark() + "," + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                } else {
                    faceDto.setFailRemark("" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                }
                return faceDto;
            } else if (hikVisionModule.faceState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                log.info("下发人脸完成, 卡号: " + new String(struFaceStatus.byCardNo).trim());
                break;
            } else {
                hikVisionModule.faceState = hikVisionModule.hCNetSDK.NET_DVR_GetLastError();
            }

        }
        if (!hikVisionModule.hCNetSDK.NET_DVR_StopRemoteConfig(hikVisionModule.faceHandle)) {
            log.error("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
        }
        faceDto.setDfaceStatus(1);
        return faceDto;
    }

    /**
     * 设备卡查询
     *
     * @param facilityDto
     * @return HumanFaceDto
     */
    @Override
    public List<CardDataDto> queryCard(FacilityDto facilityDto) {
        List<CardDataDto> list = new ArrayList<>();
        HCNetSDK.NET_DVR_CARD_COND cardCond = new HCNetSDK.NET_DVR_CARD_COND();
        cardCond.read();
        cardCond.dwSize = cardCond.size();
        //查询所有
        cardCond.dwCardNum = 0xffffffff;
        cardCond.write();
        Pointer pointer = cardCond.getPointer();
        Integer login = null;
        try {
            login = hikVisionModule.getUserHandleMap().get(facilityDto.getSerialNumber());
        } catch (NullPointerException e) {
            log.error("设备序列号为空->；" + facilityDto.toString());
            e.printStackTrace();
        }
        hikVisionModule.cardHandle = hikVisionModule.hCNetSDK.NET_DVR_StartRemoteConfig(login, HCNetSDK.NET_DVR_GET_CARD,
                pointer, cardCond.size(), null, null);
        if (hikVisionModule.cardHandle == -1) {
            log.error("建立下发卡长连接失败，错误码为" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
            System.out.println();
        }
        HCNetSDK.NET_DVR_CARD_RECORD cardRecord = new HCNetSDK.NET_DVR_CARD_RECORD();
        cardRecord.read();
        cardRecord.dwSize = cardRecord.size();
        cardRecord.write();
        while (true) {
            hikVisionModule.cardState = hikVisionModule.hCNetSDK.NET_DVR_GetNextRemoteConfig(hikVisionModule.cardHandle, cardRecord.getPointer(), cardRecord.size());
            cardRecord.read();
            if (hikVisionModule.cardState == -1) {
                log.error("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                break;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                System.out.println("获取卡参数失败");
                break;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                System.out.println("获取卡参数异常");
                break;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                try {
                    String strName = new String(cardRecord.byName, "utf-8").trim();
                    String trim = new String(cardRecord.byCardNo).trim();
                    byte byCardType = cardRecord.byCardType;
                    String trim1 = new String(cardRecord.byRes).trim();
                    byte byRes1 = cardRecord.byRes1;
                    String trim2 = new String(cardRecord.byBelongGroup).trim();
                    String trim3 = new String(String.valueOf(cardRecord.wCardRightPlan)).trim();
                    byte byCardType1 = cardRecord.byCardType;
                    int dwCardRight = cardRecord.dwCardRight;
                    System.out.println("获取卡参数成功, 卡号: " + new String(cardRecord.byCardNo).trim()
                            + ", 卡类型：" + cardRecord.byCardType
                            + ", 姓名：" + strName);

                    /*CardDataDto cardDataDto = new CardDataDto(Integer.parseInt(new String(cardRecord.byCardNo).trim()),

                            );*/
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                System.out.println("获取卡参数完成");
                break;
            }
        }
        if (!hikVisionModule.hCNetSDK.NET_DVR_StopRemoteConfig(hikVisionModule.cardHandle)) {
            System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("NET_DVR_StopRemoteConfig接口成功");
        }
        return list;
    }
    /**
     * 设备卡删除
     *
     * @param cardDataDto
     * @return HumanFaceDto
     */
    @Override
    public String deleteCard(CardDataDto cardDataDto) {
        HCNetSDK.NET_DVR_CARD_COND cardCond = new HCNetSDK.NET_DVR_CARD_COND();
        cardCond.read();
        cardCond.dwSize = cardCond.size();
        cardCond.dwCardNum = 1;
        cardCond.write();
        Pointer ptrStruCond = cardCond.getPointer();
        Integer login = null;
        try {
            login = hikVisionModule.getUserHandleMap().get(cardDataDto.getSerialNumber());
        } catch (NullPointerException e) {
            log.error("设备序列号为空->；" + cardDataDto.toString());
            e.printStackTrace();
        }
        hikVisionModule.cardHandle = hikVisionModule.hCNetSDK.NET_DVR_StartRemoteConfig(login, HCNetSDK.NET_DVR_DEL_CARD, ptrStruCond, cardCond.size(), null, null);
        if (hikVisionModule.cardHandle == -1) {
            System.out.println("建立删除卡长连接失败，错误码为" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("建立删除卡长连接成功！");
        }

        HCNetSDK.NET_DVR_CARD_SEND_DATA struCardData = new HCNetSDK.NET_DVR_CARD_SEND_DATA();
        struCardData.read();
        struCardData.dwSize = struCardData.size();

        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++) {
            struCardData.byCardNo[i] = 0;
        }
        String strCardNo = String.valueOf(cardDataDto.getCardNo());
        for (int i = 0; i < strCardNo.length(); i++) {
            struCardData.byCardNo[i] = strCardNo.getBytes()[i];
        }
        struCardData.write();
        HCNetSDK.NET_DVR_CARD_STATUS struCardStatus = new HCNetSDK.NET_DVR_CARD_STATUS();
        struCardStatus.read();
        struCardStatus.dwSize = struCardStatus.size();
        struCardStatus.write();
        IntByReference pInt = new IntByReference(0);
        while (true) {
            hikVisionModule.cardState = hikVisionModule.hCNetSDK.NET_DVR_SendWithRecvRemoteConfig(hikVisionModule.cardHandle, struCardData.getPointer(), struCardData.size(), struCardStatus.getPointer(), struCardStatus.size(), pInt);
            struCardStatus.read();
            if (hikVisionModule.cardState == -1) {
                System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
                break;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                System.out.println("配置等待");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                System.out.println("删除卡失败, 卡号: " + new String(struCardStatus.byCardNo).trim() + ", 错误码：" + struCardStatus.dwErrorCode);
                break;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                System.out.println("删除卡异常, 卡号: " + new String(struCardStatus.byCardNo).trim() + ", 错误码：" + struCardStatus.dwErrorCode);
                break;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) {
                if (struCardStatus.dwErrorCode != 0) {
                    System.out.println("删除卡成功,但是错误码" + struCardStatus.dwErrorCode + ", 卡号：" + new String(struCardStatus.byCardNo).trim());
                } else {
                    System.out.println("删除卡成功, 卡号: " + new String(struCardStatus.byCardNo).trim() + ", 状态：" + struCardStatus.byStatus);
                }
                continue;
            } else if (hikVisionModule.cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                System.out.println("删除卡完成");
                break;
            }
        }
        if (!hikVisionModule.hCNetSDK.NET_DVR_StopRemoteConfig(hikVisionModule.cardHandle)) {
            System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hikVisionModule.hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("NET_DVR_StopRemoteConfig接口成功");
        }
        return null;
    }
}
