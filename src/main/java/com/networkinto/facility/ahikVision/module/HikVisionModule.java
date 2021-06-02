package com.networkinto.facility.ahikVision.module;


import com.google.gson.Gson;
import com.networkinto.facility.ahikVision.utils.HCNetSDK;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.UrlUtils;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.LocalAuthPromptsDto;
import com.networkinto.facility.common.dto.RemoteCheckDto;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 海康设备管理
 *
 * @author cuiEnMing
 * @date 2021/5/10 14:42
 */
@Log4j2
@Component
public class HikVisionModule {
    @Resource
    private RestTemplate restTemplate;
    @Value("${server.port}")
    private int serverPort;
    public HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    /**
     * 设备与句柄的映射关系
     */
    @Getter
    private ConcurrentHashMap<String, Integer> userHandleMap = new ConcurrentHashMap<>();
    @Getter
    private List<FacilityDto> facilityDtoList = new ArrayList<>();
    /**
     * 下发卡长连接句柄
     */
    public int cardHandle = -1;
    /**
     * 下发人脸长连接句柄
     */
    public int faceHandle = -1;
    /**
     * 下发卡数据状态
     */
    public int cardState = -1;
    /**
     * 下发人脸数据状态
     */
    public int faceState = -1;
    /**
     * 报警布防句柄
     */
    int lAlarmHandle = -1;
    public HikAlarmCallBack callBacks = new HikAlarmCallBack();
    public HikReconnectionCallBack call = new HikReconnectionCallBack();

    /**
     * 设备初始化
     */
    public boolean init() {
        return hCNetSDK.NET_DVR_Init();
    }

    /**
     * 设备登录
     */
    public boolean login(FacilityDto facilityDto) {
        HCNetSDK.NET_DVR_DEVICEINFO_V30 strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int userId = hCNetSDK.NET_DVR_Login_V30(facilityDto.getIp(), (short) facilityDto.getPort(),
                facilityDto.getAccount(), facilityDto.getPassword(), strDeviceInfo);
        if (userId == -1) {
            if (IConst.IMG_SIZE!=facilityDto.getDeviceType()) {
                facilityDtoList.add(facilityDto);
            }
            log.error("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        } else {
            userHandleMap.put(facilityDto.getSerialNumber(), userId);
            log.info("登录成功！");
        }
        if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(callBacks, null)) {
            System.out.println("设置事件回调函数失败!");
        }
        if (!hCNetSDK.NET_DVR_SetExceptionCallBack_V30(5, userId, call, null)) {
            System.out.println("设置设备断线回调函数失败!");
        }
        callBacks.setRestTemplate(restTemplate);
        HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
        //智能交通布防优先级：0- 一等级（高），1- 二等级（中），2- 三等级（低）
        m_strAlarmInfo.byLevel = 1;
        //智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
        m_strAlarmInfo.byAlarmInfoType = 1;
        //布防类型(仅针对门禁主机、人证设备)：0-客户端布防(会断网续传)，1-实时布防(只上传实时数据)
        m_strAlarmInfo.byDeployType = 1;
        m_strAlarmInfo.write();
        lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(userId, m_strAlarmInfo);
        if (lAlarmHandle == -1) {
            log.error("布防失败，错误号:" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("布防成功");
        }
        return true;
    }

    public void qrCode(String serialNumber) {
        String urls = UrlUtils.getServiceInfo() + serverPort + "/hik/vision";
        log.info("接收海康二维码url：->" + urls);
        RemoteCheckDto remoteCheckDto = new RemoteCheckDto(urls,
                0, 1, 5, new LocalAuthPromptsDto("身份核验成功", "请在保安处核实身份信息",
                "身份核验成功", "非内部人员禁止入内", "身份核验成功", "非内部人员禁止入内"));
        String json = new Gson().toJson(remoteCheckDto);
        log.warn(json);
        HCNetSDK.BYTE_ARRAY ptrByte = new HCNetSDK.BYTE_ARRAY(json.length());
        System.arraycopy(json.getBytes(StandardCharsets.UTF_8), 0, ptrByte.byValue, 0, json.length());
        ptrByte.write();
        String url = IConst.qrCode.HIK_URL.getName();
       // String url="PUT /ISAPI/AccessControl/httpRemoteAuthCfg?format=json";
        HCNetSDK.NET_DVR_XML_CONFIG_INPUT struInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
        struInput.dwSize = struInput.size();
        HCNetSDK.BYTE_ARRAY ptrSetFaceAppendDataUrl = new HCNetSDK.BYTE_ARRAY(HCNetSDK.BYTE_ARRAY_LEN);
        System.arraycopy(url.getBytes(), 0, ptrSetFaceAppendDataUrl.byValue, 0, url.length());
        ptrSetFaceAppendDataUrl.write();
        struInput.lpRequestUrl = ptrSetFaceAppendDataUrl.getPointer();
        struInput.dwRequestUrlLen = url.length();
        struInput.lpInBuffer = ptrByte.getPointer();
        struInput.dwInBufferSize = ptrByte.byValue.length;
        struInput.write();
        HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
        struOutput.dwSize = struOutput.size();
        HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(HCNetSDK.ISAPI_DATA_LEN);
        struOutput.lpOutBuffer = ptrOutByte.getPointer();
        struOutput.dwOutBufferSize = HCNetSDK.ISAPI_DATA_LEN;
        HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(HCNetSDK.ISAPI_STATUS_LEN);
        struOutput.lpStatusBuffer = ptrStatusByte.getPointer();
        struOutput.dwStatusSize = HCNetSDK.ISAPI_STATUS_LEN;
        struOutput.write();
        Integer integer = userHandleMap.get(serialNumber);
        if (!hCNetSDK.NET_DVR_STDXMLConfig(integer, struInput, struOutput)) {
            log.info("PUT error_code:" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            log.info("PUT 成功:" + hCNetSDK.NET_DVR_GetLastError());
        }
    }
}
