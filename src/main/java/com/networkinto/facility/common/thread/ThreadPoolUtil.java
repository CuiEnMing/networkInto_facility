package com.networkinto.facility.common.thread;


import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cuiEnMing
 * @Desc 线程池管理
 */
public class ThreadPoolUtil {
    /**
     * 登录大华设备
     */
    public static ExecutorService newAjCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new BasicThreadFactory.Builder().namingPattern("device-aj-login-pool-%d").daemon(true).build());
    }

    /**
     * 登录海康设备
     */
    public static ExecutorService newHkCachedThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new BasicThreadFactory.Builder().namingPattern("device-hk-login-pool-%d").daemon(true).build());
    }

    /**
     * 开启二维码穿透
     */
    public static ExecutorService newInterfaceThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(), new BasicThreadFactory.Builder().namingPattern("device-interface-pool-%d").daemon(true).build());
    }
}
