package com.networkinto.facility.ahikVision.module;

import com.alibaba.fastjson.JSONArray;
import com.networkinto.facility.ahikVision.utils.HCNetSDK;
import com.networkinto.facility.common.dto.FacilityDto;
import com.sun.jna.Pointer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.io.UnsupportedEncodingException;
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
    public  int cardState = -1;
    /**
     * 下发人脸数据状态
     */
    public int faceState = -1;
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
            return true;
        }
    }

    /**
     * 查询所有卡号
     */
    public void getAllCard(FacilityDto facilityDto) {
        cardHandle = -1;
        HCNetSDK.NET_DVR_CARD_COND struCardCond = new HCNetSDK.NET_DVR_CARD_COND();
        struCardCond.read();
        struCardCond.dwSize = struCardCond.size();
        /**
         * 查询所有
         */
        struCardCond.dwCardNum = 0xffffffff;
        struCardCond.write();
        Pointer ptrStruCond = struCardCond.getPointer();

        cardHandle = hCNetSDK.NET_DVR_StartRemoteConfig(userHandleMap.get(facilityDto.getSerialNumber()), HCNetSDK.NET_DVR_GET_CARD, ptrStruCond, struCardCond.size(), null, null);
        if (cardHandle == -1) {
            System.out.println("建立下发卡长连接失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("建立下发卡长连接成功！");
        }

        HCNetSDK.NET_DVR_CARD_RECORD struCardRecord = new HCNetSDK.NET_DVR_CARD_RECORD();
        struCardRecord.read();
        struCardRecord.dwSize = struCardRecord.size();
        struCardRecord.write();
        JSONArray cards = new JSONArray();
        while (true) {
            cardState = hCNetSDK.NET_DVR_GetNextRemoteConfig(cardHandle, struCardRecord.getPointer(), struCardRecord.size());
            struCardRecord.read();
            if (cardState == -1) {
                System.out.println("NET_DVR_SendWithRecvRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
                break;
            } else if (cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_NEEDWAIT) {
                System.out.println("配置等待");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            } else if (cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FAILED) {
                System.out.println("获取卡参数失败");
                break;
            } else if (cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_EXCEPTION) {
                System.out.println("获取卡参数异常");
                break;
            } else if (cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_SUCCESS) { //NET_SDK_GET_NEXT_STATUS_SUCCESS
                try {
                    System.out.println("获取卡参数成功, 卡号: " + new String(struCardRecord.byCardNo).trim()
                            + ", 卡类型：" + struCardRecord.byCardType
                            + ", 姓名：" + new String(struCardRecord.byName, "utf-8").trim());

                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                continue;
            } else if (cardState == HCNetSDK.NET_SDK_CONFIG_STATUS_FINISH) {
                System.out.println("获取卡参数完成");
                break;
            }
        }

        if (!hCNetSDK.NET_DVR_StopRemoteConfig(cardHandle)) {
            System.out.println("NET_DVR_StopRemoteConfig接口调用失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println("NET_DVR_StopRemoteConfig接口成功");
        }

        //return cards;
    }

}
