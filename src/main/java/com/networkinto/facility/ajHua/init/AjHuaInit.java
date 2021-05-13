package com.networkinto.facility.ajHua.init;

import com.networkinto.facility.ajHua.module.AjHuaModule;
import com.networkinto.facility.ajHua.utils.NetSDKLib;
import com.networkinto.facility.ajHua.utils.ToolKits;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.dto.FaceInfoDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.thread.ThreadPoolUtil;
import com.sun.jna.Pointer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * 大华设备初始化
 *
 * @author cuiEnMing
 * @date 2021/5/13 14:53
 */
@Log4j2
@Component
@Order(value = 1)
public class AjHuaInit  implements CommandLineRunner {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AjHuaModule ajHuaModule;
    @Resource
    private ToolKits toolKits;
    private DisConnect disConnect = new DisConnect();
    @Getter
    private FaceCallBack faceCallBack = new FaceCallBack();
    private static HaveReConnect haveReConnect = new HaveReConnect();

    @Override
    public void run(String... args) throws Exception {
        if (!ajHuaModule.init(disConnect, haveReConnect)) {
            log.error("设备sdk初始化失败");
            return;
        }
        ParameterizedTypeReference<List<FacilityDto>> type = new ParameterizedTypeReference<List<FacilityDto>>() {
        };
        ResponseEntity<List<FacilityDto>> responseEntity = restTemplate.exchange(IConst.AJ_HUA_DEVICE , HttpMethod.GET, null, type);
        List<FacilityDto> list = responseEntity.getBody();
        for (FacilityDto deviceDto : list) {
            if (deviceDto.getDeviceType() != IConst.SUCCEED_CODE) {
                continue;
            }
            if (StringUtil.isNotBlank(deviceDto.getId()) && StringUtil.isNotBlank(deviceDto.getAccount())
                    && StringUtil.isNotBlank(deviceDto.getPassword()) && StringUtil.isNotBlank(deviceDto.getIp())
                    && (deviceDto.getPort() > 0)) {
                //开启线程池
                if (deviceDto.getIp().equals("172.16.11.240")){
                    System.out.println("ssssss");
                }
                Runnable loginRunnable = () -> {
                    //登录设备
                    if (!ajHuaModule.login(deviceDto.getIp(), deviceDto.getPort(), deviceDto.getAccount(), deviceDto.getPassword(), deviceDto.getSerialNumber())) {
                        List<String> failDevice = IConst.failDevice ;
                        if (!failDevice.contains(deviceDto.getIp())) {
                            IConst.failDevice .add(deviceDto.getIp());
                        }
                        //.add(deviceDto.getIp());
                        log.error("设备登录失败 ip->{}，端口->{},用户名->{},密码->{}", deviceDto.getIp(), deviceDto.getPort(),
                                deviceDto.getAccount(), deviceDto.getPassword());
                    } else {
                        /**
                         *二维码穿透
                         * */
                        ajHuaModule.qrCode(deviceDto);
                        /**
                         * 人脸订阅
                         * */
                        ajHuaModule.realLoadPicture(0,
                                faceCallBack, deviceDto);
                    }
                };
                //提交线程
                ThreadPoolUtil.newAjCachedThreadPool().submit(loginRunnable);

            } else {
                List<String> failDevice = IConst.failDevice ;
                if (!failDevice.contains(deviceDto.getIp())) {
                    IConst.failDevice .add(deviceDto.getIp());
                }
                log.warn("设备基础信息异常：->" + deviceDto);
            }
        }

    }
    /**
     * 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
     */
    private class DisConnect implements NetSDKLib.fDisConnect {
        @Override
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            List<String> failDevice =  IConst.failDevice;
            if (!failDevice.contains(pchDVRIP)) {
                IConst.failDevice .add(pchDVRIP);
            }
            //todo  断线提示
            log.info("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
        }
    }

    /**
     * 网络连接恢复，设备重连成功回调
     */
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        @Override
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            //todo 重连提示
            log.info("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
            List<String> failDevice = IConst.failDevice ;
            if (failDevice.contains(pchDVRIP)) {
                IConst.failDevice .remove(pchDVRIP);
            }
        }
    }

    /**
     * 人脸订阅回调
     */
    private class FaceCallBack implements NetSDKLib.fAnalyzerDataCallBack {
        @Override
        public int invoke(NetSDKLib.LLong lAnalyzerHandle, int dwAlarmType,
                          Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize,
                          Pointer dwUser, int nSequence, Pointer reserved) {
            if (pAlarmInfo == null) {
                return 0;
            }
            switch (dwAlarmType) {
                case NetSDKLib.EVENT_IVS_ACCESS_CTL:   //todo 门禁事件
                    NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO event = new NetSDKLib.DEV_EVENT_ACCESS_CTL_INFO();
                    toolKits.GetPointerData(pAlarmInfo, event);
                    FaceInfoDto accessEvent = new FaceInfoDto();
                    accessEvent.szCardName = new String(event.szCardName).trim();
                    //10位以后是设备id (userId 是10位uuid+设备id生成的)
                    accessEvent.userId = new String(event.szUserID).trim().substring(10);
                    accessEvent.cardNo = new String(event.szCardNo).trim();
                    accessEvent.eventTime = event.UTC.toStringTime();
                    accessEvent.openDoorMethod = event.emOpenMethod;
                    //  NetSDKLib.NET_ACCESS_DOOROPEN_METHOD ;  //开门方式
                    log.info(accessEvent.toString());
                    break;
                default:
                    break;
            }
            return 0;
        }
    }
}
