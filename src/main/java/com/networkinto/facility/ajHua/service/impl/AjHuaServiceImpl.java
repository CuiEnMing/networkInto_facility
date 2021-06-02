package com.networkinto.facility.ajHua.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.networkinto.facility.ajHua.module.AjHuaModule;
import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.ajHua.utils.JsonResult;
import com.networkinto.facility.ajHua.utils.NetSDKLib;
import com.networkinto.facility.ajHua.utils.ToolKits;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.UrlUtils;
import com.networkinto.facility.common.dto.*;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cuiEnMing
 * @Desc 门禁设备用户管理 （卡号 ，图片）
 */
@Log4j2
@Service("DeviceService")
public class AjHuaServiceImpl implements AjHuaService {
    @Resource
    private ToolKits toolKits;
    @Resource
    private AjHuaModule ajHuaModule;
    @Resource
    private RestTemplate restTemplate;
    /**
     * 序列号与房间号的关联
     */
    @Getter
    private final ConcurrentHashMap<String, byte[]> serialAndUserIDMap = new ConcurrentHashMap<>();

    /**
     * 验证二维码权限
     *
     * @param hikQrCodeDto 二维码
     * @return JsonResult
     */
    @Override
    public RemoteCheck checkQrCode(HikQrCodeDto hikQrCodeDto) {
        RemoteCheck remoteCheck = new RemoteCheck();
        String res = HttpUtil.createPost(IConst.URL_PREFIX + IConst.qrCode.ROAD_SERVICE.getName() + IConst.qrCode.CHECK_CODE.getName())
                .setMethod(Method.POST)
                .contentType("application/x-www-form-urlencoded")
                .form("qrcode", hikQrCodeDto.getQrCode())
                .form("casecode", "1")
                .form("casename", "1")
                .form("description", "1")
                .timeout(5000)
                .execute()
                .body();
        JsonObject asJsonObject = new JsonParser().parse(res).getAsJsonObject();
        int code = asJsonObject.get("code").getAsInt();
        String message = asJsonObject.get("message").getAsString();
        //路路通返回结果 1成功
        Integer result = 1;
        if (result.equals(code)) {
            remoteCheck.setCheckResult("success");
            //todo  验证通过需存储用户出行轨迹
            JsonObject data = asJsonObject.get("data").getAsJsonObject();
            String name = data.get("name").toString().replaceAll("\"", "");
            String mobile = data.get("mobile").toString().replaceAll("\"", "");
            LocalDateTime time = LocalDateTime.now();
            String format = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            EventDto eventDto = new EventDto();
            eventDto.setEventtime(format);
            eventDto.setName(name);
            eventDto.setMobile(mobile);
            eventDto.setSn(hikQrCodeDto.getSn());
            eventDto.setCheckType(1);
        } else {
            remoteCheck.setCheckResult("failed");
        }
        remoteCheck.setSerialNo(Integer.parseInt(hikQrCodeDto.getSerialNo()));
        return remoteCheck;
    }

    /**
     * 关闭二维码穿透
     *
     * @param facilityDto 设备编号
     * @return InterfaceReturnsDto
     */
    @Override
    public InterfaceReturnsDto closeQrCOde(FacilityDto facilityDto) {
        Map<Object, Object> map = ajHuaModule.qrCodeParams(facilityDto.getSerialNumber(), "", IConst.qrCode.CLOSE_QRCODE.getName());
        String qrCodeUrl = UrlUtils.qrCodeUrl(facilityDto.getIp());
        String url = qrCodeUrl + IConst.qrCode.URL;
        return restTemplate.postForObject(url, map, InterfaceReturnsDto.class);
    }

    /**
     * 设备登出
     *
     * @param deviceDto
     */
    @Override
    public JsonResult<String> outDevice(String deviceDto) {
        NetSDKLib.LLong lLong = ajHuaModule.getHandleMap().get(deviceDto);
        if (lLong.longValue() == 0) {
            log.info("无需登出");
        }
        NetSDKLib.LLong m_hLoginHandle = ajHuaModule.m_hLoginHandle;
        log.info("登录句柄为" + m_hLoginHandle);
        boolean bRet = ajHuaModule.netsdk.CLIENT_Logout(ajHuaModule.m_hLoginHandle);
        if (bRet) {
            lLong.setValue(0);
            return JsonResult.ok("设备登出成功", null);
        } else {
            return JsonResult.error("登出失败!", toolKits.getErrorCodePrint(), ajHuaModule.netsdk.CLIENT_GetLastError() & 0x7fffffff);
        }
    }

    @Override
    public HumanFaceDto addPicture(byte[] fileBytes, String fileName, HumanFaceDto dto) {
        // 门禁卡记录集信息
        String uuID = UUID.randomUUID().toString().trim().replaceAll("-", "").substring(0, 10) + dto.getDeviceId();
        byte[] userID = uuID.getBytes();
        String cardId = dto.getCardNo().toString();
        byte[] cardNo = cardId.getBytes();
        List<CardDataDto> list = queryCard(dto.getSerialNumber());
        if (IConst.SUCCEED < list.size()) {
            for (CardDataDto cardDataDto : list) {
                if (String.valueOf(cardDataDto.getCardNo()).equals(cardId)) {
                    deleteCard(cardDataDto);
                    log.info("{}卡号删除 重新下发", cardId);
                    deleteFaceInfo(cardDataDto.getUserId(), cardDataDto.getSerialNumber());
                }
            }
        }
        //获得对于句柄
        NetSDKLib.LLong lLong = ajHuaModule.getHandleMap().get(dto.getSerialNumber());
        NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD card = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD();
        System.arraycopy(userID, 0, card.szUserID, 0, userID.length);
        System.arraycopy(cardNo, 0, card.szCardNo, 0, cardNo.length);
        card.nDoorNum = 1;
        card.sznDoors[0] = 0;
        try {
            card.szCardName = fileName.getBytes("GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        NetSDKLib.NET_TIME startTime = new NetSDKLib.NET_TIME();
        startTime.setTime(dto.getStartTime().getYear(),
                dto.getStartTime().get(ChronoField.MONTH_OF_YEAR), dto.getStartTime().getDayOfMonth(),
                dto.getStartTime().getHour(), dto.getStartTime().getMinute(), dto.getExpiresTime().getSecond());
        NetSDKLib.NET_TIME endTime = new NetSDKLib.NET_TIME();
        endTime.setTime(dto.getExpiresTime().getYear(),
                dto.getExpiresTime().get(ChronoField.MONTH_OF_YEAR), dto.getExpiresTime().getDayOfMonth(),
                dto.getExpiresTime().getHour(), dto.getExpiresTime().getMinute(), dto.getExpiresTime().getSecond());
        card.stuValidStartTime = startTime;
        card.stuValidEndTime = endTime;
        NetSDKLib.NET_CTRL_RECORDSET_INSERT_PARAM inParam = new NetSDKLib.NET_CTRL_RECORDSET_INSERT_PARAM();
        inParam.stuCtrlRecordSetInfo.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        inParam.stuCtrlRecordSetInfo.nBufLen = card.size();
        inParam.stuCtrlRecordSetInfo.pBuf = new Memory(card.size());
        toolKits.SetStructDataToPointer(card, inParam.stuCtrlRecordSetInfo.pBuf, 0);
        Pointer pointer = new Memory(inParam.size());
        toolKits.SetStructDataToPointer(inParam, pointer, 0);
        // 插入指纹必须用  CTRLTYPE_CTRL_RECORDSET_INSERTEX，不能用 CTRLTYPE_CTRL_RECORDSET_INSERT
        boolean res = ajHuaModule.netsdk.CLIENT_ControlDevice(lLong, NetSDKLib.CtrlType.CTRLTYPE_CTRL_RECORDSET_INSERTEX, pointer, 5000);
        if (!res) {
            String errorCodeShow = toolKits.getErrorCodeShow();
            log.error("添加用户ID 发生异常 异常信息->{}", errorCodeShow);
            dto.setCardStatus(-1);
            dto.setNoNum(dto.getNoNum() + 1);
            dto.setDfaceStatus(-1);
            if (StringUtil.isNotBlank(dto.getFailRemark())) {
                dto.setFailRemark(dto.getFailRemark() + "," + toolKits.getErrorCodeShow());
            } else {
                dto.setFailRemark(toolKits.getErrorCodeShow());
            }

            return dto;
        } else {
            dto.setCardStatus(1);
        }
        //增加人脸图片
        if (fileBytes.length > IConst.SUCCEED) {
            NetSDKLib.NET_IN_ADD_FACE_INFO inAddFaceInfo = new NetSDKLib.NET_IN_ADD_FACE_INFO();
            System.arraycopy(userID, 0, inAddFaceInfo.szUserID, 0, userID.length);
            inAddFaceInfo.stuFaceInfo.nFacePhoto = 1;
            inAddFaceInfo.stuFaceInfo.nFacePhotoLen[0] = (int) new Memory(fileBytes.length).getSize();
            inAddFaceInfo.stuFaceInfo.pszFacePhotoArr[0].pszFacePhoto = new Memory((int) new Memory(fileBytes.length).getSize());
            inAddFaceInfo.stuFaceInfo.pszFacePhotoArr[0].pszFacePhoto.write(0, fileBytes, 0, (int) new Memory(fileBytes.length).getSize());
            inAddFaceInfo.stuFaceInfo.nRoom = 1;
            inAddFaceInfo.stuFaceInfo.stuValidDateStart = startTime;
            inAddFaceInfo.stuFaceInfo.stuValidDateEnd = endTime;
            System.arraycopy(userID, 0, inAddFaceInfo.stuFaceInfo.szRoomNoArr[0].szRoomNo, 0, userID.length);
            NetSDKLib.NET_OUT_ADD_FACE_INFO outAddFaceInfo = new NetSDKLib.NET_OUT_ADD_FACE_INFO();
            Pointer outFaceParam = new Memory(outAddFaceInfo.size());
            toolKits.SetStructDataToPointer(outAddFaceInfo, outFaceParam, 0);
            Pointer inFace = new Memory(inAddFaceInfo.size());
            toolKits.SetStructDataToPointer(inAddFaceInfo, inFace, 0);
            boolean result = ajHuaModule.netsdk.CLIENT_FaceInfoOpreate(lLong, NetSDKLib.EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_ADD,
                    inFace, outFaceParam, 10000);
            if (!result) {
                String errorCodeShow = toolKits.getErrorCodeShow();
                log.error("添加用户人脸信息时 发生异常 异常原因{}", errorCodeShow);
                dto.setNoNum(dto.getNoNum() + 1);
                dto.setDfaceStatus(-1);
                dto.setCardStatus(-1);
                if (StringUtil.isNotBlank(dto.getFailRemark())) {
                    dto.setFailRemark(dto.getFailRemark() + "," + toolKits.getErrorCodeShow());
                } else {
                    dto.setFailRemark(toolKits.getErrorCodeShow());
                }
            } else {
                dto.setDfaceStatus(1);
            }
        }
        serialAndUserIDMap.put(dto.getSerialNumber(), userID);
        return dto;
    }

    /**
     * 卡信息删除
     *
     * @param cardDataDto
     * @return boolean
     */
    @Override
    public JsonResult<String> deleteCard(CardDataDto cardDataDto) {
        NetSDKLib.NET_CTRL_RECORDSET_PARAM remove = new NetSDKLib.NET_CTRL_RECORDSET_PARAM();
        remove.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        remove.pBuf = new IntByReference(cardDataDto.getRecNo()).getPointer();

        remove.write();
        boolean result = ajHuaModule.netsdk.CLIENT_ControlDevice(ajHuaModule.getHandleMap().get(cardDataDto.getSerialNumber()),
                NetSDKLib.CtrlType.CTRLTYPE_CTRL_RECORDSET_REMOVE, remove.getPointer(), 5000);
        remove.read();
        if (!result) {
            log.error("卡信息删除失败 ：" + toolKits.getErrorCodeShow());
        }
        return JsonResult.ok("ok", "");
    }

    /**
     * 查询信息
     * * @return
     */
    @Override
    public List<CardDataDto> queryCard(String serialNumber) {
        //开始查询记录
        NetSDKLib.NET_IN_FIND_RECORD_PARAM inParam = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM outParam = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        //门禁卡
        inParam.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        NetSDKLib.FIND_RECORD_ACCESSCTLCARD_CONDITION condition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARD_CONDITION();
        boolean bRet = ajHuaModule.netsdk.CLIENT_FindRecord(ajHuaModule.getHandleMap().get(serialNumber), inParam, outParam, 10000);
        if (!bRet) {
            log.error(toolKits.getErrorCodeShow());

        }
        NetSDKLib.LLong lFindHandle = outParam.lFindeHandle;
        //Query查询所有数据
        List<CardDataDto> list = queryData(lFindHandle, serialNumber);
        //结束查询
        boolean success = ajHuaModule.netsdk.CLIENT_FindRecordClose(lFindHandle);
        if (!success) {
            log.error(toolKits.getErrorCodeShow());
        }
        return list;
    }

    /**
     * 循环遍历获取卡数据
     *
     * @param lFindHandle
     * @param serialNumber
     */
    public List<CardDataDto> queryData(NetSDKLib.LLong lFindHandle, String serialNumber) {
        List<CardDataDto> list = new ArrayList<>();
        while (true) {
            int max = 20;
            //query the next batch of data 查询下一组数据
            NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM inParam = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
            NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM outParam = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();

            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[] cards = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[max];
            for (int i = 0; i < max; i++) {
                cards[i] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD();
            }

            outParam.pRecordList = new Memory(cards[0].size() * max);
            outParam.nMaxRecordNum = max;
            inParam.lFindeHandle = lFindHandle;
            inParam.nFileCount = max;
            toolKits.SetStructArrToPointerData(cards, outParam.pRecordList);
            boolean result = ajHuaModule.netsdk.CLIENT_FindNextRecord(inParam, outParam, 10000);
            //获取数据
            toolKits.GetPointerDataToStructArr(outParam.pRecordList, cards);
            NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD card;
            for (int i = 0; i < outParam.nRetRecordNum; i++) {
                CardDataDto cardDataDto = new CardDataDto();
                //获取到卡数据
                card = cards[i];
                //有指纹信息,则获取指纹数据
                card.bEnableExtended = 1;
                card.stuFingerPrintInfoEx.nPacketLen = 2048;
                card.stuFingerPrintInfoEx.pPacketData = new Memory(2048);
                String trim = new String(card.szCardNo).trim();
                cardDataDto.setCardNo(trim);
                cardDataDto.setRecNo(card.nRecNo);
                cardDataDto.setUserId(new String(card.szUserID).trim());
                cardDataDto.setSerialNumber(serialNumber);
                list.add(cardDataDto);
            }
            return list;
        }
    }

    public static NetSDKLib.NET_TIME setNetTime(LocalDateTime time) {
        NetSDKLib.NET_TIME netTime = new NetSDKLib.NET_TIME();
        netTime.setTime(time.getYear(), time.get(ChronoField.MONTH_OF_YEAR), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond());
        return netTime;
    }

    /**
     * 检查下卡号是否存在
     * true:不存在
     * false:存在
     *
     * @param serialNumber
     * @param cardDataDto
     * @return
     */
    public boolean checkCardNo(byte[] cardNo, String serialNumber, CardDataDto cardDataDto) {
        if (cardNo.length == 0) {
            return false;
        }
        //check whether the card number already exists查询一下卡号是否已经存在
        NetSDKLib.NET_IN_FIND_RECORD_PARAM inParam = new NetSDKLib.NET_IN_FIND_RECORD_PARAM();
        inParam.emType = NetSDKLib.EM_NET_RECORD_TYPE.NET_RECORD_ACCESSCTLCARD;
        //查询条件
        NetSDKLib.FIND_RECORD_ACCESSCTLCARD_CONDITION condition = new NetSDKLib.FIND_RECORD_ACCESSCTLCARD_CONDITION();
        //卡号查询有效
        condition.abCardNo = 1;
        if (cardNo.length > condition.szCardNo.length - 1) {
            return false;
        }
        System.arraycopy(cardNo, 0, condition.szCardNo, 0, cardNo.length);
        inParam.pQueryCondition = new Memory(condition.size());
        toolKits.SetStructDataToPointer(condition, inParam.pQueryCondition, 0);
        NetSDKLib.NET_OUT_FIND_RECORD_PARAM outParam = new NetSDKLib.NET_OUT_FIND_RECORD_PARAM();
        NetSDKLib.LLong lLong = ajHuaModule.getHandleMap().get(serialNumber);
        boolean startFind = ajHuaModule.netsdk.CLIENT_FindRecord(lLong, inParam, outParam, 5000);
        if (!startFind) {
            return false;
        }
        //查询卡号是否已存在
        int max = 1;
        NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM inNextParam = new NetSDKLib.NET_IN_FIND_NEXT_RECORD_PARAM();
        inNextParam.lFindeHandle = outParam.lFindeHandle;
        inNextParam.nFileCount = max;
        NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM outNextParam = new NetSDKLib.NET_OUT_FIND_NEXT_RECORD_PARAM();
        outNextParam.nMaxRecordNum = max;
        NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[] card = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD[1];
        card[0] = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD();
        outNextParam.pRecordList = new Memory(card[0].size() * max);
        toolKits.SetStructArrToPointerData(card, outNextParam.pRecordList);
        ajHuaModule.netsdk.CLIENT_FindNextRecord(inNextParam, outNextParam, 5000);
        if (outNextParam.nRetRecordNum != 0) {
            //卡号已存在
            //停止查询
            ajHuaModule.netsdk.CLIENT_FindRecordClose(outParam.lFindeHandle);
            return false;
        }
        //停止查询
        ajHuaModule.netsdk.CLIENT_FindRecordClose(outParam.lFindeHandle);
        return true;
    }

    /**
     * 删除人脸(单个删除)
     *
     * @param userId 用户ID
     */
    public boolean deleteFaceInfo(String userId, String serialNumber) {
        int emType = NetSDKLib.EM_FACEINFO_OPREATE_TYPE.EM_FACEINFO_OPREATE_REMOVE;

        /**
         * 入参
         */
        NetSDKLib.NET_IN_REMOVE_FACE_INFO inRemove = new NetSDKLib.NET_IN_REMOVE_FACE_INFO();

        // 用户ID
        System.arraycopy(userId.getBytes(), 0, inRemove.szUserID, 0, userId.getBytes().length);

        /**
         *  出参
         */
        NetSDKLib.NET_OUT_REMOVE_FACE_INFO outRemove = new NetSDKLib.NET_OUT_REMOVE_FACE_INFO();
        inRemove.write();
        outRemove.write();
        boolean bRet = ajHuaModule.netsdk.CLIENT_FaceInfoOpreate(ajHuaModule.getHandleMap().get(serialNumber), emType, inRemove.getPointer(), outRemove.getPointer(), 5000);
        inRemove.read();
        outRemove.read();
        if (!bRet) {
            log.error("删除人脸失败 ， 失败原因{}", toolKits.getErrorCodeShow());
        }
        return bRet;
    }
}
