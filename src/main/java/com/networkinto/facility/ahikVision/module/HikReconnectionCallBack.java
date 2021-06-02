package com.networkinto.facility.ahikVision.module;

import com.networkinto.facility.ahikVision.utils.HCNetSDK;
import com.sun.jna.Pointer;
import lombok.extern.log4j.Log4j2;

/**
 * @author cuiEnMing
 * @date 2021/5/28 16:14
 */
@Log4j2
public class HikReconnectionCallBack implements HCNetSDK.FExceptionCallBack {
    @Override
    public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {

        log.info("设备网络发生变化");
    }
}
