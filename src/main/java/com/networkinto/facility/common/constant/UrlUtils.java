package com.networkinto.facility.common.constant;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author cuiEnMing
 * @date 2021/5/19 14:36
 */
public class UrlUtils {
    static String urlPrefix = IConst.URL_PREFIX;
    static String symbol = IConst.SYMBOL;

    /**
     * 图片转换
     *
     * @param imgUrl
     * @return byte[]
     * @throws IOException
     */
    public static byte[] imgConvert(String imgUrl) throws IOException {
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

    /**
     * 智慧小区接口
     */
    public static String wisdomCommunityUrl() {
        String ip = IConst.wisdomCommunity.IP.getName();
        String port = IConst.wisdomCommunity.PORT.getName();
        String url = urlPrefix + ip + symbol + port;
        return url;
    }

    /**
     * 设备二维码穿透
     */
    public static String qrCodeUrl(String ip) {
        String port = IConst.qrCode.PORT.getName();
        String url = urlPrefix + ip + symbol + port;
        return url;
    }
}
