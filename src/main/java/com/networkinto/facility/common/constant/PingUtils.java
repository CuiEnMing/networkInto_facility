package com.networkinto.facility.common.constant;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * @author anlei
 */
@Log4j2
public class PingUtils {

    public static boolean ping(String host, Integer port, int timeOut) {
        // 原文链接：https://blog.csdn.net/qq_33377979/java/article/details/77938030
        if (port == null || port.intValue() == 0) {
            return ping(host, timeOut);
        }
        Socket s = new Socket();
        SocketAddress add = new InetSocketAddress(host, port);
        try {
            // 超时3秒
            s.connect(add, timeOut);
        } catch (IOException e) {
            //异常需要处理的业务逻辑
            return false;
        } finally {
            try {
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static boolean ping(String ipAddress, int timeOut) {
        try {
            return InetAddress.getByName(ipAddress).isReachable(timeOut);
        } catch (Exception e) {
            return false;
        }

    }

}
