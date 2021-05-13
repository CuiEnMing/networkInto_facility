package com.networkinto.facility.ajHua.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回值
 * @author cuiEnMing
 * @Desc
 */
@Data
public class JsonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码 0:操作失败;1操作成功
     */
    private int code;

    /**
     * 文中提示信息，由服务器返回后，在H5显示
     */
    private String message;


    /**
     * 返回json 的数据部分
     */
    private T data;

    /**
     * 无参构造
     */
    public JsonResult() {
    }

    /**
     * 有参构造
     *
     * @param code 状态参数
     * @param msg  返回语句
     * @param data 返回数据
     */
    public JsonResult(int code, String msg, T data) {
        this.code = code;
        this.message = msg;
        this.data = data;
    }


    /**
     * 创建一个操作成功的 JsonResult
     *
     * @param message 消息字符串
     * @param data    数据
     * @return 操作成功的 JsonResult
     */
    public static <T> JsonResult<T> ok(String message, T data) {
        JsonResult<T> jsonResult = new JsonResult<T>();
        jsonResult.setCode(0);
        jsonResult.setMessage(message);
        jsonResult.setData(data);
        return jsonResult;
    }

    /**
     * 创建一个操作成功的 JsonResult
     *
     * @param message 消息字符串
     * @return 操作成功的 JsonResult
     */
    public static <T> JsonResult<T> okNoData(String message) {
        JsonResult<T> jsonResult = new JsonResult<T>();
        // 和R兼容,设置0表示成功
        jsonResult.setCode(0);
        jsonResult.setMessage(message);
        return jsonResult;
    }

    /**
     * 创建一个操作失败的 JsonResult
     *
     * @param message 消息字符串
     * @param data    数据
     * @param errCode 错误代码
     * @return 操作成功的 JsonResult
     */
    public static <T> JsonResult<T> error(String message, T data, int errCode) {
        JsonResult<T> jsonResult = new JsonResult<T>();
        jsonResult.setMessage(message);
        jsonResult.setData(data);
        jsonResult.setCode(errCode);
        return jsonResult;
    }

}
