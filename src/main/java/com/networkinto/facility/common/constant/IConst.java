package com.networkinto.facility.common.constant;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    Integer ONE = 1;
    Integer TWO = 2;
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
    String AJ_HUA_DEVICE = "http://172.16.11.214:8080/business/device/external/getDeviceList?status=0";
    /**
     * 人脸下发数据
     * todo 暂时路径
     */
    String FACE_DATA = "http://172.16.11.214:8080/business/deviceTask/external/getDeviceTaskList";
    /**
     * 人脸下发结果
     * todo 暂时路径
     */
    String FACE_RESULT = "http://172.16.11.214:8080/business/deviceTask/external/updateDeviceTask";
    /**
     * 人脸抠图
     */
    String TEST = "https://face.techzhl.com/face/crop_face?face_url=";
    /**
     * 人脸抠图
     */
    String TEST1 = "&type=base64";
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



    public static final String DEFAULT_SVM_PATH = "model/1602828039163_svm.xml";
    public static final String DEFAULT_ANN_PATH = "model/ann.xml";
    public static final String DEFAULT_ANN_CN_PATH = "model/ann_cn.xml";
    public static final String DEFAULT_ANN_GREEN_PATH = "model/ann_green.xml";
    public static final String DEFAULT_FACE_MODEL_PATH = "model/haarcascade_frontalface_default.xml";
    public static final String DEFAULT_PLATE_MODEL_PATH = "model/harrcascade_frontplate.xml";

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

    /**
     * 数据格式转换
     *
     * @param imgUrl
     * @return Mat
     * @throws IOException
     * @throws
     */
    public static Mat inputStream2Mat(String imgUrl) throws IOException {
        /*将网络资源地址传给,即赋值给url*/
        URL url = new URL(imgUrl);
        /*此为联系获得网络资源的固定格式用法，以便后面的in变量获得url截取网络资源的输入流*/
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla / 4.76");
        InputStream inputStream = connection.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = bis.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.flush();
        os.close();
        bis.close();
        Mat encoded = new Mat(1, os.size(), 0);
        encoded.put(0, 0, os.toByteArray());
        Mat decoded = Imgcodecs.imdecode(encoded, -1);
        encoded.release();
        inputStream.close();
        return decoded;
    }

}
