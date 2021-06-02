package com.networkinto.facility.common.constant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 判断成员变量是否为空
 *
 * @author enMing.cui
 * @version 1.0
 * @date 2021/5/11 16:50
 */
public class BeanUtil {

    public static List<String> isObjectFieldEmpty(Object obj) throws IllegalAccessException {
        //得到类对象
        Class<?> clazz = obj.getClass();
        //得到属性集合
        Field[] fs = clazz.getDeclaredFields();
        List<String> list = new ArrayList<>();
        //遍历属性
        for (Field field : fs) {
            //设置属性是可以访问的（私有的也可以）
            field.setAccessible(true);
            if (field.get(obj) == null || field.get(obj) == "" || "null".equalsIgnoreCase(String.valueOf(field.get(obj)))) {
                String name = field.getName();
                list.add(name);
            }
        }
        return list;
    }
}
