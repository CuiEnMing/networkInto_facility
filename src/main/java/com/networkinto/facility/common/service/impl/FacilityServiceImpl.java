package com.networkinto.facility.common.service.impl;

import cn.hutool.extra.cglib.CglibUtil;
import com.networkinto.facility.ahikVision.module.HikVisionModule;
import com.networkinto.facility.ajHua.module.AjHuaModule;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.PingUtils;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.FacilityStatusDto;
import com.networkinto.facility.common.service.FacilityService;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author cuiEnMing
 * @date 2021/5/18 9:33
 */
@Service("FacilityService")
public class FacilityServiceImpl implements FacilityService {
    @Resource
    @Getter
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
            facilityStatusDto.setStatus(IConst.SUCCEED);
            if (IConst.SUCCEED == facilityStatusDto.getType()) {
                if (!ajHuaModule.login(facilityStatusDto.getIp(), facilityStatusDto.getPort(), facilityStatusDto.getAccount(),
                        facilityStatusDto.getPassword(), facilityStatusDto.getSerialNumber())) {
                    //设备登录失败
                    facilityStatusDto.setStatus(IConst.TWO);
                }
            } else if (IConst.ONE == facilityStatusDto.getType()) {
                FacilityDto facilityDto = new FacilityDto();
                CglibUtil.copy(facilityStatusDto, facilityDto);
                if (!hikVisionModule.login(facilityDto)) {
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
