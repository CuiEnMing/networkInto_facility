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
import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.HumanFaceDto;
import com.networkinto.facility.common.dto.InterfaceReturnsDto;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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
     * @param qrCode 二维码
     * @return JsonResult
     */
    @Override
    public JsonResult<String> checkQrCode(String qrCode) {
        String res = HttpUtil.createPost(IConst.API_SERVER + IConst.CHECK_CODE_URL)
                .setMethod(Method.POST)
                .contentType("application/x-www-form-urlencoded")
                .form("qrcode", qrCode)
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
            //todo  验证通过需存储用户出行轨迹
            return JsonResult.ok(message, "");
        }
        return JsonResult.error(message, "", code);
    }
    /**
     * 关闭二维码穿透
     *
     * @param facilityDto 设备编号
     * @return InterfaceReturnsDto
     */
    @Override
    public InterfaceReturnsDto closeQrCOde(FacilityDto facilityDto) {
        Map<Object, Object> map = ajHuaModule.qrCodeParams(facilityDto.getSerialNumber(), "", IConst.CLOSE_QRCODE_PUSH);
        String url = facilityDto.getIp() + ":" + IConst.QR_CODE_URL;
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
        //获得对于句柄
        NetSDKLib.LLong lLong = ajHuaModule.getHandleMap().get(dto.getSerialNumber());
        NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD card = new NetSDKLib.NET_RECORDSET_ACCESS_CTL_CARD();
        System.arraycopy(userID, 0, card.szUserID, 0, userID.length);
        System.arraycopy(cardNo, 0, card.szCardNo, 0, cardNo.length);
        card.nDoorNum = 1;
        card.sznDoors[0] = 0;
        card.szCardName = fileName.getBytes(StandardCharsets.UTF_8);
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
        if (fileBytes.length > IConst.SUCCEED_CODE) {
            NetSDKLib.NET_IN_ADD_FACE_INFO inAddFaceInfo = new NetSDKLib.NET_IN_ADD_FACE_INFO();
            NetSDKLib.NET_TIME startTime = setNetTime(dto.getStartTime());
            NetSDKLib.NET_TIME endTime = setNetTime(dto.getExpiresTime());
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
            log.error("卡信息删除失败 ："+toolKits.getErrorCodeShow());
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
                cardDataDto.setCardNo(Integer.parseInt(new String(card.szCardNo).trim()));
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
}
