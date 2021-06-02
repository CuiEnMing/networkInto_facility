package com.networkinto.facility.common.constant;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * @author cuiEnMing
 * @date 2021/5/19 14:36
 */
@Component
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
     * url转file
     *
     * @param fileUrl
     * @return byte[]
     * @throws IOException
     */
    public static File getFileByUrl(String fileUrl) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedOutputStream stream = null;
        InputStream inputStream = null;
        File file = null;
        try {
            URL imageUrl = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            file = File.createTempFile("file", fileUrl.substring(fileUrl.lastIndexOf("."), fileUrl.length()));
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fileOutputStream);
            stream.write(outStream.toByteArray());
        } catch (Exception e) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (stream != null) {
                    stream.close();
                }
                outStream.close();
            } catch (Exception e) {
            }
        }
        return file;
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

    /**
     * 获得本机服务器路径
     */
    public static String getServiceInfo() {
        String hostAddress = "";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String url = IConst.URL_PREFIX + hostAddress + IConst.SYMBOL ;
        return url;
    }
}
