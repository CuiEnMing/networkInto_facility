package com.networkinto.facility.common.constant;

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
    Integer SUCCEED = 0;
    Integer ONE = 0;
    Integer TWO = 0;
    /**
     * URL前缀
     */
    String URL_PREFIX = "http://";
    /**
     * 符号
     */
    String SYMBOL = ":";


    /**
     * 对接设备 开启二维码穿透路径
     */
    enum qrCode {
        /**
         * 开启二维码穿透的访问端口
         */
        PORT("80"),

        /**
         * 开启二维码穿透的访问路径
         */
        URL("/api/cmd"),
        /**
         * 路路通
         */
        ROAD_SERVICE("sfm.ksecard.cn:10080"),
        /**
         * 验证二维码路径
         */
        CHECK_CODE("/qrcservice/rest/qrcode/securityValidate"),
        /**
         * 开启二维码
         */
        OPEN_QRCODE("open_qrcode_push"),
        /**
         * 关闭二维码
         */
        CLOSE_QRCODE("close_qrcode_push");
        private final String name;

        qrCode(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * 对接智慧小区接口
     */
    enum wisdomCommunity {
        /**
         * 项目端口号 （智慧小区）
         */
        PORT("8080"),
        /**
         * 项目对接IP（智慧小区）
         */
        IP("172.16.11.214"),

        /**
         * 设备接口路径（智慧小区）
         */
        FACILITY_INTERFACE("/business/device/external/getDeviceList?status=0"),
        /**
         * 人脸下发数据接口（智慧小区）
         */
        FACE_INTERFACE("/business/deviceTask/external/getDeviceTaskList"),

        /**
         * 人脸下发结果（智慧小区）
         */
        FACE_RESULT("/business/deviceTask/external/updateDeviceTask");
        private final String name;

        wisdomCommunity(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * openCv
     */
    enum openCv {
        DEFAULT_SVM_PATH("model/1602828039163_svm.xml"),
        DEFAULT_ANN_PATH("model/ann.xml"),
        DEFAULT_ANN_CN_PATH("model/ann_green.xml"),
        DEFAULT_FACE_MODEL_PATH("model/haarcascade_frontalface_default.xml"),
        DEFAULT_PLATE_MODEL_PATH("model/harrcascade_frontplate.xml");
        private final String name;

        openCv(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
