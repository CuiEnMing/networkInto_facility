package com.networkinto.facility.ahikVision.module;

import com.networkinto.facility.ahikVision.utils.HCNetSDK;
import com.sun.jna.Pointer;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author cuiEnMing
 * @date 2021/5/19 10:34
 */
public class HikAlarmCallBack implements HCNetSDK.FMSGCallBack_V31 {
    /**
     * todo 空指针待解决
     */
    private RestTemplate restTemplate = new RestTemplate();
    /**
     * 以下下设备监控参数
     */
    public String sAlarmType;
    public HCNetSDK.NET_DVR_ALARMER alarm;
    public String[] newRow;
    public Pointer alarmInfo;
    public DateFormat dateFormat;
    public Date today;
    public String[] sIP;

    @Override
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER alarms, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        //报警信息回调函数
        alarmCallBack(lCommand, alarms, pAlarmInfo, dwBufLen, pUser);
        return true;
    }


    public void alarmCallBack(int lCommand, HCNetSDK.NET_DVR_ALARMER alarms, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        try {
            alarm = alarms;
            newRow = new String[3];
            alarmInfo = pAlarmInfo;
            //报警时间
            today = new Date();
            dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            sAlarmType = new String("lCommand=0x") + Integer.toHexString(lCommand);
            //lCommand是传的报警类型
            switch (lCommand) {
                case HCNetSDK.COMM_ALARM_V40:
                    commAlarmV40();
                    break;
                case HCNetSDK.COMM_ALARM_RULE:
                    commAlarmRule();
                    break;
                case HCNetSDK.COMM_UPLOAD_FACESNAP_RESULT:
                    commUploadFaceResult();
                    break;
                //门禁主机报警信息
                case HCNetSDK.COMM_ALARM_ACS:
                    commAlarmAcs();
                    break;
                //身份证信息
                case HCNetSDK.COMM_ID_INFO_ALARM:
                    commIdInfoAlarm();
                    break;
                default:
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(alarm.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    break;
            }
        } catch (Exception ex) {
        }
    }

    /**
     * 身份证信息报警
     */
    private void commIdInfoAlarm() {
        HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
        strIDCardInfo.write();
        Pointer pIDCardInfo = strIDCardInfo.getPointer();
        pIDCardInfo.write(0, alarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
        strIDCardInfo.read();
        sAlarmType = sAlarmType + "：门禁身份证刷卡信息，身份证号码：" + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() + "，姓名：" +
                new String(strIDCardInfo.struIDCardCfg.byName).trim() + "，报警主类型：" + strIDCardInfo.dwMajor + "，报警次类型：" + strIDCardInfo.dwMinor;
        newRow[0] = dateFormat.format(today);
        //报警类型
        newRow[1] = sAlarmType;
        //报警设备IP地址
        sIP = new String(alarm.sDeviceIP).split("\0", 2);
        newRow[2] = sIP[0];
        //身份证图片
        if (strIDCardInfo.dwPicDataLen > 0) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
            String newName = sf.format(new Date());
            FileOutputStream fout;
            try {
                String filename = ".\\pic\\" + new String(alarm.sDeviceIP).trim() +
                        "_byCardNo[" + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() +
                        "_" + newName + "_IDInfoPic.jpg";
                fout = new FileOutputStream(filename);
                //将字节写入文件
                long offset = 0;
                ByteBuffer buffers = strIDCardInfo.pPicData.getByteBuffer(offset, strIDCardInfo.dwPicDataLen);
                byte[] bytes = new byte[strIDCardInfo.dwPicDataLen];
                buffers.rewind();
                buffers.get(bytes);
                fout.write(bytes);
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        //抓拍图片
        if (strIDCardInfo.dwCapturePicDataLen > 0) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
            String newName = sf.format(new Date());
            FileOutputStream fout;
            try {
                String filename = ".\\pic\\" + new String(alarm.sDeviceIP).trim() +
                        "_byCardNo[" + new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() +
                        "_" + newName + "_IDInfoCapturePic.jpg";
                fout = new FileOutputStream(filename);
                //将字节写入文件
                long offset = 0;
                ByteBuffer buffers = strIDCardInfo.pCapturePicData.getByteBuffer(offset, strIDCardInfo.dwCapturePicDataLen);
                byte[] bytes = new byte[strIDCardInfo.dwCapturePicDataLen];
                buffers.rewind();
                buffers.get(bytes);
                fout.write(bytes);
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 门禁主机报警信息
     */
    private void commAlarmAcs() {
        HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
        strACSInfo.write();
        Pointer pACSInfo = strACSInfo.getPointer();
        pACSInfo.write(0, alarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
        strACSInfo.read();
        sAlarmType = sAlarmType + "：门禁主机报警信息，卡号：" + new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + "，卡类型：" +
                strACSInfo.struAcsEventInfo.byCardType + "，报警主类型：" + strACSInfo.dwMajor + "，报警次类型：" + strACSInfo.dwMinor;
        byte byDeviceNo = strACSInfo.struAcsEventInfo.byDeviceNo;
        int dwEmployeeNo = strACSInfo.struAcsEventInfo.dwEmployeeNo;
        byte byCardReaderKind = strACSInfo.struAcsEventInfo.byCardReaderKind;
        newRow[0] = dateFormat.format(today);
        //报警类型
        newRow[1] = sAlarmType;
        //报警设备IP地址
        sIP = new String(alarm.sDeviceIP).split("\0", 2);
        newRow[2] = sIP[0];
        if (strACSInfo.dwPicDataLen > 0) {
            addImg(strACSInfo);
        }
    }


    /**
     * 实时人脸抓拍上传
     */
    private void commUploadFaceResult() {
        HCNetSDK.NET_VCA_FACESNAP_RESULT strFaceSnapInfo = new HCNetSDK.NET_VCA_FACESNAP_RESULT();
        strFaceSnapInfo.write();
        Pointer pFaceSnapInfo = strFaceSnapInfo.getPointer();
        pFaceSnapInfo.write(0, alarmInfo.getByteArray(0, strFaceSnapInfo.size()), 0, strFaceSnapInfo.size());
        strFaceSnapInfo.read();
        sAlarmType = sAlarmType + "：人脸抓拍上传，人脸评分：" + strFaceSnapInfo.dwFaceScore + "，年龄段："
                + strFaceSnapInfo.struFeature.byAgeGroup + "，性别：" + strFaceSnapInfo.struFeature.bySex;
        newRow[0] = dateFormat.format(today);
        //报警类型
        newRow[1] = sAlarmType;
        //报警设备IP地址
        sIP = new String(strFaceSnapInfo.struDevInfo.struDevIP.sIpV4).split("\0", 2);
        newRow[2] = sIP[0];
        //设置日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        // new Date()为获取当前系统时间
        String time = df.format(new Date());
        //人脸图片写文件
        try {
            FileOutputStream small = new FileOutputStream(System.getProperty("user.dir") + "\\pic\\" + time + "small.jpg");
            FileOutputStream big = new FileOutputStream(System.getProperty("user.dir") + "\\pic\\" + time + "big.jpg");
            if (strFaceSnapInfo.dwFacePicLen > 0) {
                try {
                    byte[] batse = strFaceSnapInfo.pBuffer1.getByteArray(0, strFaceSnapInfo.dwFacePicLen);
                    small.write(strFaceSnapInfo.pBuffer1.getByteArray(0, strFaceSnapInfo.dwFacePicLen), 0, strFaceSnapInfo.dwFacePicLen);
                    small.close();
                } catch (IOException ex) {
                }
            }
            if (strFaceSnapInfo.dwFacePicLen > 0) {
                try {
                    big.write(strFaceSnapInfo.pBuffer2.getByteArray(0, strFaceSnapInfo.dwBackgroundPicLen),
                            0, strFaceSnapInfo.dwBackgroundPicLen);
                    big.close();
                } catch (IOException ex) {
                }
            }
        } catch (FileNotFoundException ex) {
        }
    }

    /**
     * 行为分析报警
     */
    private void commAlarmRule() {
        HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
        strVcaAlarm.write();
        Pointer pVcaInfo = strVcaAlarm.getPointer();
        pVcaInfo.write(0, alarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
        strVcaAlarm.read();
        switch (strVcaAlarm.struRuleInfo.wEventTypeEx) {
            case 1:
                sAlarmType = sAlarmType + new String("：穿越警戒面") + "，" +
                        "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                        "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                        "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                        "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                break;
            case 2:
                sAlarmType = sAlarmType + new String("：目标进入区域") + "，" +
                        "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                        "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                        "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                        "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                break;
            case 3:
                sAlarmType = sAlarmType + new String("：目标离开区域") + "，" +
                        "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                        "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                        "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                        "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                break;
            default:
                sAlarmType = sAlarmType + new String("：其他行为分析报警，事件类型：")
                        + strVcaAlarm.struRuleInfo.wEventTypeEx +
                        "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                        "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                        "_byIvmsChannel:" + strVcaAlarm.struDevInfo.byIvmsChannel +
                        "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                break;
        }
        if (strVcaAlarm.dwPicDataLen > 0) {
            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
            String newName = sf.format(new Date());
            FileOutputStream fout;
            try {
                //todo 保存图片需 修改
                fout = new FileOutputStream(".\\pic\\" + new String(alarm.sDeviceIP).trim()
                        + "wEventTypeEx[" + strVcaAlarm.struRuleInfo.wEventTypeEx + "]_" + newName + "_vca.jpg");
                //将字节写入文件
                long offset = 0;
                ByteBuffer buffers = strVcaAlarm.pImage.getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                byte[] bytes = new byte[strVcaAlarm.dwPicDataLen];
                buffers.rewind();
                buffers.get(bytes);
                fout.write(bytes);
                fout.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 未知
     */
    private void commAlarmV40() {
        HCNetSDK.NET_DVR_ALARMINFO_V40 alarmInfoV40 = new HCNetSDK.NET_DVR_ALARMINFO_V40();
        alarmInfoV40.write();
        Pointer pInfoV40 = alarmInfoV40.getPointer();
        pInfoV40.write(0, alarmInfo.getByteArray(0, alarmInfoV40.size()), 0, alarmInfoV40.size());
        alarmInfoV40.read();
        dwAlarmType(alarmInfoV40);
        newRow[0] = dateFormat.format(today);
        //报警类型
        newRow[1] = sAlarmType;
        //报警设备IP地址
        sIP = new String(alarm.sDeviceIP).split("\0", 2);
        newRow[2] = sIP[0];
    }

    /**
     * 告警类型
     */
    private void dwAlarmType(HCNetSDK.NET_DVR_ALARMINFO_V40 alarmInfoV40) {
        switch (alarmInfoV40.struAlarmFixedHeader.dwAlarmType) {
            case 0:
                alarmInfoV40.struAlarmFixedHeader.ustruAlarm.setType(HCNetSDK.struIOAlarm.class);
                alarmInfoV40.read();
                sAlarmType = sAlarmType + new String("：信号量报警") + "，" + "报警输入口：" +
                        alarmInfoV40.struAlarmFixedHeader.ustruAlarm.struioAlarm.dwAlarmInputNo;
                break;
            case 1:
                sAlarmType = sAlarmType + new String("：硬盘满");
                break;
            case 2:
                sAlarmType = sAlarmType + new String("：信号丢失");
                break;
            case 3:
                alarmInfoV40.struAlarmFixedHeader.ustruAlarm.setType(HCNetSDK.struAlarmChannel.class);
                alarmInfoV40.read();
                int iChanNum = alarmInfoV40.struAlarmFixedHeader.ustruAlarm.sstrualarmChannel.dwAlarmChanNum;
                sAlarmType = sAlarmType + new String("：移动侦测") + "，" + "报警通道个数：" + iChanNum + "，" + "报警通道号：";
                for (int i = 0; i < iChanNum; i++) {
                    byte[] byChannel = alarmInfoV40.pAlarmData.getByteArray(i * 4, 4);
                    int iChanneNo = 0;
                    for (int j = 0; j < 4; j++) {
                        int ioffset = j * 8;
                        int iByte = byChannel[j] & 0xff;
                        iChanneNo = iChanneNo + (iByte << ioffset);
                    }
                    sAlarmType = sAlarmType + "+ch[" + iChanneNo + "]";
                }
                break;
            case 4:
                sAlarmType = sAlarmType + new String("：硬盘未格式化");
                break;
            case 5:
                sAlarmType = sAlarmType + new String("：读写硬盘出错");
                break;
            case 6:
                sAlarmType = sAlarmType + new String("：遮挡报警");
                break;
            case 7:
                sAlarmType = sAlarmType + new String("：制式不匹配");
                break;
            case 8:
                sAlarmType = sAlarmType + new String("：非法访问");
                break;
            default:

                break;
        }
    }

    /**
     * 保存图片
     *
     * @param strACSInfo
     */
    private void addImg(HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
        String newName = sf.format(new Date());
        FileOutputStream fileOutputStream = null;
        byte[] bytes = new byte[8];
        String filename = "D:\\pic\\" + new String(alarm.sDeviceIP).trim() +
                "_byCardNo[" + new String(strACSInfo.struAcsEventInfo.byCardNo).trim() +
                "_" + newName + "_Acs.jpg";
        File f = new File(filename);
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(filename);
            //将字节写入文件
            long offset = 0;
            ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(offset, strACSInfo.dwPicDataLen);
            bytes = new byte[strACSInfo.dwPicDataLen];
            buffers.rewind();
            buffers.get(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
    }
}
