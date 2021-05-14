package com.networkinto.facility.ahikVision.module;

import com.alibaba.fastjson.JSONArray;
import com.networkinto.facility.ahikVision.utils.HCNetSDK;
import com.networkinto.facility.common.dto.FacilityDto;
import com.sun.jna.Pointer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 海康设备管理
 *
 * @author cuiEnMing
 * @date 2021/5/10 14:42
 */
@Log4j2
@Component
public class HikVisionModule {
    public HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    /**
     * 设备与句柄的映射关系
     */
    @Getter
    private ConcurrentHashMap<String, Integer> userHandleMap = new ConcurrentHashMap<>();
    /**
     * 用户句柄
     */
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
    //报警回调函数实现
    public static callBack callBacks;

    /**
     * 设备初始化
     */
    public boolean init() {
        return hCNetSDK.NET_DVR_Init();
    }

    /**
     * 设备登录
     */
    public boolean login(FacilityDto deviceDto) {

        HCNetSDK.NET_DVR_DEVICEINFO_V30 strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int userId = hCNetSDK.NET_DVR_Login_V30(deviceDto.getIp(), (short) deviceDto.getPort(), deviceDto.getAccount(), deviceDto.getPassword(), strDeviceInfo);
        if (userId == -1) {
            log.error("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        } else {
            userHandleMap.put(deviceDto.getSerialNumber(), userId);
            log.info("登录成功！");
        }


        callBacks = new callBack();
        Pointer pUser = null;
        if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(callBacks, pUser)) {
            System.out.println("设置回调函数失败!");
        }
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

    public class callBack implements HCNetSDK.FMSGCallBack_V31 {
        //报警信息回调函数
        @Override
        public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
            AlarmDataHandle.alarm(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }
    }
}
