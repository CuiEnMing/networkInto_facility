package com.networkinto.facility.common.service.impl;

import com.networkinto.facility.ahikVision.module.HikVisionModule;
import com.networkinto.facility.ajHua.module.AjHuaModule;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.PingUtils;
import com.networkinto.facility.common.dto.FacilityStatusDto;
import com.networkinto.facility.common.service.FacilityService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.swing.*;

/**
 * @author cuiEnMing
 * @date 2021/5/18 9:33
 */
@Service("FacilityService")
public class FacilityServiceImpl implements FacilityService {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AjHuaModule ajHuaModule;
    @Resource
    private HikVisionModule hikVisionModule;

    /**
     * 验证设备状态
     *
     * @param facilityStatusDto
     */
    @Override
    public void checkFacility(FacilityStatusDto facilityStatusDto) {
        if (PingUtils.ping(facilityStatusDto.getIp(), facilityStatusDto.getPort(), 3000)) {
            facilityStatusDto.setStatus(IConst.SUCCEED_CODE);
            if (IConst.SUCCEED_CODE == facilityStatusDto.getType()) {
                if (!ajHuaModule.login(facilityStatusDto.getIp(), facilityStatusDto.getPort(), facilityStatusDto.getAccount(),
                        facilityStatusDto.getPassword(), facilityStatusDto.getSerialNumber())) {
                    //设备登录失败
                    facilityStatusDto.setStatus(IConst.TWO);
                }
            } else if (IConst.ONE == facilityStatusDto.getType()) {
                if (!hikVisionModule.login(facilityStatusDto.getIp(), facilityStatusDto.getPort(), facilityStatusDto.getAccount(),
                        facilityStatusDto.getPassword(), facilityStatusDto.getSerialNumber())) {
                    //设备登录失败
                    facilityStatusDto.setStatus(IConst.TWO);
                }
                //设备断线
            }
        } else {
            facilityStatusDto.setStatus(IConst.ONE);
        }

    }
}
