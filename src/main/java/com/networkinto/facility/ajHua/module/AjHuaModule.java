package com.networkinto.facility.ajHua.module;

import com.networkinto.facility.ajHua.utils.NetSDKLib;
import com.networkinto.facility.ajHua.utils.ToolKits;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.UrlUtils;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.InterfaceReturnsDto;
import com.networkinto.facility.common.thread.ThreadPoolUtil;
import com.sun.jna.Pointer;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 大化设备处理公共类
 *
 * @author cuiEnMing
 */
@Log4j2
@Component
public class AjHuaModule {
    @Resource
    private RestTemplate restTemplate;
    @Value("${server.port}")
    private int serverPort;
    @Resource
    private ToolKits toolKits;
    /**
     * 设备与句柄的映射关系
     */
    @Getter
    private ConcurrentHashMap<String, NetSDKLib.LLong> handleMap = new ConcurrentHashMap<>();
    final static Lock REENTRANT_LOCK = new ReentrantLock();
    public NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;
    /**
     * 设备信息
     */
    public NetSDKLib.NET_DEVICEINFO_Ex deviceInfoEx = new NetSDKLib.NET_DEVICEINFO_Ex();
    /**
     * 登陆句柄
     */
    public NetSDKLib.LLong m_hLoginHandle = new NetSDKLib.LLong(0);
    private boolean bInit = false;
    private boolean bLogopen = false;
    /**
     * 用于人脸检测
     */
    private static int groupId = 0;
    /**
     * 人脸图
     */
    private static BufferedImage bufferedImage = null;
    /**
     * 全景图
     */
    private static BufferedImage globalBufferedImage = null;

    /**
     * 设备初始化
     */
    public boolean init(NetSDKLib.fDisConnect disConnect, NetSDKLib.fHaveReConnect haveReConnect) {
        bInit = netsdk.CLIENT_Init(disConnect, null);
        if (!bInit) {
            System.out.println("Initialize SDK failed");
            return false;
        }
        //打开日志，可选
        NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        File path = new File("./sdklog/");
        if (!path.exists()) {
            path.mkdir();
        }
        String logPath = path.getAbsoluteFile().getParent() + "\\sdklog\\" + toolKits.getDate() + ".log";
        setLog.nPrintStrategy = 0;
        setLog.bSetFilePath = 1;
        System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
        System.out.println(logPath);
        setLog.bSetPrintStrategy = 1;
        bLogopen = netsdk.CLIENT_LogOpen(setLog);
        if (!bLogopen) {
            //todo
            System.err.println("Failed to open NetSDK log");
        }
        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
        // 此操作为可选操作，但建议用户进行设置
        netsdk.CLIENT_SetAutoReconnect(haveReConnect, null);
        //设置登录超时时间和尝试次数，可选登录请求响应超时时间设置为5S
        int waitTime = 5000;
        //登录时尝试建立链接1次
        int tryTimes = 1;
        netsdk.CLIENT_SetConnectTime(waitTime, tryTimes);
        // 设置更多网络参数，NET_PARAM的nWaittime，nConnectTryNum成员与CLIENT_SetConnectTime
        // 接口设置的登录设备超时时间和尝试次数意义相同,可选
        NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
        // 登录时尝试建立链接的超时时间
        netParam.nConnectTime = 1000;
        // 设置子连接的超时时间
        netParam.nGetConnInfoTime = 3000;
        netsdk.CLIENT_SetNetworkParam(netParam);
        return true;
    }

    /**
     * \if ENGLISH_LANG
     * Login Device
     * \else
     * 登录设备
     * \endif
     */
    public boolean login(String ip, int port, String user, String password, String serialNumber) {
        //入参
        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstInParam.nPort = port;
        pstInParam.szIP = ip.getBytes();
        pstInParam.szPassword = password.getBytes();
        pstInParam.szUserName = user.getBytes();
        //出参
        NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstOutParam.stuDeviceInfo = deviceInfoEx;
        m_hLoginHandle = netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
        log.info("设备登录句柄 ：" + m_hLoginHandle);
        //将句柄存入ConcurrentHashMap中
        handleMap.put(serialNumber, m_hLoginHandle);
        return m_hLoginHandle.longValue() == 0 ? false : true;
    }

    public NetSDKLib.LLong realLoadPicture(int channel, NetSDKLib.fAnalyzerDataCallBack callback, FacilityDto facilityDto) {
        // 是否需要图片
        int bNeedPicture = 1;
        NetSDKLib.LLong lLong = handleMap.get(facilityDto.getSerialNumber());
        NetSDKLib.LLong m_hAttachHandle = netsdk.CLIENT_RealLoadPictureEx(lLong, channel,
                NetSDKLib.EVENT_IVS_ALL, bNeedPicture, callback, null, null);
        log.info("ConcurrentHashMap中对应句柄:->" + lLong);
        log.info("设备登录句柄与map句柄比较：->" + m_hLoginHandle.equals(lLong));
        if (m_hAttachHandle.longValue() == 0) {
            log.info("CLIENT_RealLoadPictureEx Failed, Error:" + toolKits.getErrorCodePrint());
        } else {
            log.info("通道[" + channel + "]订阅成功！");
        }
        return m_hAttachHandle;
    }

    /**
     * \if ENGLISH_LANG
     * CleanUp
     * \else
     * 清除环境
     * \endif
     */
    public void cleanup() {
        if (bLogopen) {
            netsdk.CLIENT_LogClose();
        }
        if (bInit) {
            netsdk.CLIENT_Cleanup();
        }
    }

    /**
     * \if ENGLISH_LANG
     * Logout Device
     * \else
     * 登出设备
     * \endif
     */
    public boolean logout() {
        if (m_hLoginHandle.longValue() == 0) {
            return false;
        }
        boolean bRet = netsdk.CLIENT_Logout(m_hLoginHandle);
        if (bRet) {
            m_hLoginHandle.setValue(0);
        }
        return bRet;
    }

    /**
     * @return
     * @Description 向设备发送请求 开启二维码穿透
     * @params ip  设备ip
     * @author cuiEnMing
     * @since 2021/4/26 9:35
     */
    public void qrCode(FacilityDto facilityDto) {
        String hostAddress = "";
        REENTRANT_LOCK.lock();
        try {
            //项目部署环境ip
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取本机ip失败");
            e.printStackTrace();
        } finally {
            REENTRANT_LOCK.unlock();
        }
        //组装传输数据
        String finalHostAddress = hostAddress;
        Runnable runnable = () -> {
            String url = IConst.URL_PREFIX + finalHostAddress + IConst.SYMBOL + serverPort + "/facilityDto/manage/" + facilityDto.getSerialNumber() + "/QR_CODE";
            // val 设备序列号  val1 接受二维码 的接口
            Map<Object, Object> objectObjectMap = qrCodeParams(facilityDto.getSerialNumber(), url, IConst.qrCode.OPEN_QRCODE.getName());
            handleMap.put(facilityDto.getSerialNumber(), m_hLoginHandle);
            try {
                //设备接口路径
                String qrCodeUrl = UrlUtils.qrCodeUrl(facilityDto.getIp()) + IConst.qrCode.URL;
                log.info("url :" + qrCodeUrl);
                InterfaceReturnsDto interfaceReturnsDto = restTemplate.postForObject(qrCodeUrl, objectObjectMap, InterfaceReturnsDto.class);
                if (IConst.SUCCEED.equals(interfaceReturnsDto.getCode())) {
                    log.info("设备开启二维码穿透成功 ->" + qrCodeUrl);
                } else {
                    log.error("设备开启二维码穿透失败 ->" + qrCodeUrl);
                }
            } catch (RestClientException e) {
                log.error("设备开启二维码穿透失败" + UrlUtils.qrCodeUrl(facilityDto.getIp()) + IConst.qrCode.URL);
                e.printStackTrace();
            }
        };
        ThreadPoolUtil.newInterfaceThreadPool().submit(runnable);
    }

    /**
     * 组装参数 ConcurrentHashMap 保证多线程下map线程安全
     */
    public ConcurrentHashMap<Object, Object> qrCodeParams(String val, String val1, String val2) {
        ConcurrentHashMap<Object, Object> map = new ConcurrentHashMap<>(8);
        map.put("device_number", val);
        map.put("url", val1);
        map.put("cmd", val2);
        return map;
    }

    /**
     * 保存人脸检测事件图片
     *
     * @param pBuffer        抓拍图片信息
     * @param dwBufSize      抓拍图片大小
     * @param faceDetectInfo 人脸检测事件信息
     */
    public void saveFaceDetectPic(Pointer pBuffer, int dwBufSize,
                                  NetSDKLib.DEV_EVENT_FACEDETECT_INFO faceDetectInfo) {
        File path = new File("D:\\FaceDetection\\");
        if (!path.exists()) {
            path.mkdir();
        }

        if (pBuffer == null || dwBufSize <= 0) {
            return;
        }

        // 小图的 stuObject.nRelativeID 来匹配大图的 stuObject.nObjectID，来判断是不是 一起的图片
        /**
         *保存人脸图
         */
        if (groupId != faceDetectInfo.stuObject.nRelativeID) {
            bufferedImage = null;
            groupId = faceDetectInfo.stuObject.nObjectID;

            String strGlobalPicPathName = path + "\\" + faceDetectInfo.UTC.toStringTitle() + "_FaceDetection_Global.jpg";
            byte[] bufferGlobal = pBuffer.getByteArray(0, dwBufSize);
            ByteArrayInputStream byteArrInputGlobal = new ByteArrayInputStream(bufferGlobal);

            try {
                globalBufferedImage = ImageIO.read(byteArrInputGlobal);
                if (globalBufferedImage != null) {
                    File globalFile = new File(strGlobalPicPathName);
                    if (globalFile != null) {
                        ImageIO.write(globalBufferedImage, "jpg", globalFile);
                    }
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            /**
             *保存人脸图
             */
        } else if (groupId == faceDetectInfo.stuObject.nRelativeID) {
            if (faceDetectInfo.stuObject.stPicInfo != null) {
                String strPersonPicPathName = path + "\\" + faceDetectInfo.UTC.toStringTitle() + "_FaceDetection_Person.jpg";
                byte[] bufferPerson = pBuffer.getByteArray(0, dwBufSize);
                ByteArrayInputStream byteArrInputPerson = new ByteArrayInputStream(bufferPerson);

                try {
                    bufferedImage = ImageIO.read(byteArrInputPerson);
                    if (bufferedImage != null) {
                        File personFile = new File(strPersonPicPathName);
                        if (personFile != null) {
                            ImageIO.write(bufferedImage, "jpg", personFile);
                        }
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }


}
