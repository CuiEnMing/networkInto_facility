package com.networkinto.facility.common.constant;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.networkinto.facility.common.dto.EventDto;
import lombok.extern.log4j.Log4j2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * 保存图片
 * 格式 D:\认证类型\年\唯一标识\月\日+时+分+ip\图片
 *
 * @author cuiEnMing
 * @date 2021/5/28 10:01
 */
@Log4j2
public class FileUtils {
    public static EventDto saveFile(EventDto eventDto, byte[] bytes) {
        if (IConst.SUCCEED == bytes.length) {
            log.warn("图片数组为空 保存图片失败");
            return eventDto;
        }
        //图片保存位置 D
        String windowsPrefix = IConst.IMG_WINDOWS_PREFIX;
        //根据系统获得文件分割符 windows 与 linux 分隔符相反 \
        String fileSeparator = FileUtil.FILE_SEPARATOR;
        Calendar calendar = Calendar.getInstance();
        //年
        int year = calendar.get(Calendar.YEAR);
        //月 +1 当前月
        int march = calendar.get(Calendar.MARCH) + IConst.ONE;
        //日
        int date = calendar.get(Calendar.DATE);
        //时
        int hour = calendar.get(Calendar.HOUR);
        //分
        int minute = calendar.get(Calendar.MINUTE);
        //ip
        String ip = eventDto.getIp();
        //文件名
        String fileName = "";
        FileOutputStream fileOutputStream = null;
        if (!StrUtil.hasBlank(eventDto.getIdentity())) {
            //身份证不为空 代表是人证认证 D:\认证类型\年\唯一标识\月\图片
            fileName = windowsPrefix + fileSeparator + "identity" + year +
                    fileSeparator + eventDto.getIdentity() + fileSeparator + march + "月" + fileSeparator +
                    date + "日" + hour + "时" + minute + "分" + bytes.length + "->" + ip + ".jpg";
        } else if (!StrUtil.hasBlank(eventDto.getMobile())) {
            //手机号不为空 代表是二维码认证 D:\认证类型\年\唯一标识\月\图片
            fileName = windowsPrefix + fileSeparator + "qrCOde" + year +
                    fileSeparator + eventDto.getMobile() + fileSeparator + march + "月" + fileSeparator +
                    date + "日" + hour + "时" + minute + "分->" + bytes.length + ip + ".jpg";
        } else {
            if (!StrUtil.hasBlank(eventDto.getName())) {
                //最后是人脸认证
                fileName = windowsPrefix + fileSeparator + "face" + year +
                        fileSeparator + eventDto.getName() + fileSeparator + march + "月" + fileSeparator +
                        date + "日" + hour + "时" + minute + "分" + bytes.length + ip + ".jpg";
            }
        }
        try {
            fileOutputStream = new FileOutputStream(fileName);
            fileOutputStream.write(bytes);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException ioException) {
                log.error("图片保存异常");
                ioException.printStackTrace();
            }
        }
        eventDto.setImgUrl(fileName);
        return eventDto;
    }
}
