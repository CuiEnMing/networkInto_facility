package com.networkinto.facility.common.constant;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cuiEnMing
 * @date 2021/5/13 14:13
 */
public interface IConst {
    List<String> failDevice = Collections.synchronizedList(new ArrayList<>());
    /**
     * 成功代码
     */
    Integer SUCCEED_CODE = 0;
    /**
     * 开启二维码穿透的访问端口
     */
    Integer QR_CODE_PORT = 80;
    /**
     * 开启二维码穿透的访问路径
     */
    String QR_CODE_URL = ":80/api/cmd";
    /**
     * 大华设备列表
     * todo 暂时路径
     */
    String AJ_HUA_DEVICE = "http://172.16.11.214:8070/business/device/external/getDeviceList?status=0";
    /**
     * 人脸下发数据
     * todo 暂时路径
     */
    String FACE_DATA = "http://172.16.11.214:8070/business/deviceTask/external/getDeviceTaskList";
    /**
     * 人脸下发结果
     * todo 暂时路径
     */
    String FACE_RESULT = "http://172.16.11.214:8070/business/deviceTask/external/updateDeviceTask";
    /**
     * 路路通
     */
    String API_SERVER = "http://sfm.ksecard.cn:10080";
    /**
     * 验证二维码路径
     */
    String CHECK_CODE_URL = "/qrcservice/rest/qrcode/securityValidate";
    /**
     * 开启二维码
     */
    String OPEN_QRCODE_PUSH = "open_qrcode_push";
    /**
     * 关闭二维码
     */
    String CLOSE_QRCODE_PUSH = "close_qrcode_push";

    /**
     * 图片转换
     *
     * @param imgUrl
     * @return byte[]
     * @throws
     */
    public static byte[] imgConvert(String imgUrl) throws Exception {
        /*将网络资源地址传给,即赋值给url*/
        URL url = new URL(imgUrl);
        /*此为联系获得网络资源的固定格式用法，以便后面的in变量获得url截取网络资源的输入流*/
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla / 4.76");
        InputStream is = connection.getInputStream();
        //定义字节数组大小
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        int rc = 0;
        while ((rc = is.read(bytes, 0, 100)) > 0) {
            swapStream.write(bytes, 0, rc);
        }
        bytes = swapStream.toByteArray();
        return bytes;
    }
}
