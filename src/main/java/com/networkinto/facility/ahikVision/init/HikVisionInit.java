package com.networkinto.facility.ahikVision.init;

import com.networkinto.facility.ahikVision.module.HikVisionModule;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.thread.ThreadPoolUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.config.Order;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cuiEnMing
 * @Desc
 * @data 2021/5/10 13:56
 */
@Log4j2
@Component
@Order(value = 2)
public class HikVisionInit implements CommandLineRunner {
    @Resource
    private HikVisionModule hikVisionModule;
    @Resource
    private RestTemplate restTemplate;

    @Override
    public void run(String... args) {
        if (hikVisionModule.init()) {
            log.info("海康sdk 初始化成功");
        } else {
            log.info("海康sdk 初始化失败");
            return;
        }
        ParameterizedTypeReference<List<FacilityDto>> type = new ParameterizedTypeReference<List<FacilityDto>>() {
        };
        ResponseEntity<List<FacilityDto>> responseEntity = restTemplate.exchange(IConst.AJ_HUA_DEVICE, HttpMethod.GET, null, type);
        List<FacilityDto> list = responseEntity.getBody();
        for (FacilityDto facilityDto : list) {
            if (facilityDto.getDeviceType() == IConst.SUCCEED_CODE) {
                continue;
            }
            if (StringUtil.isNotBlank(facilityDto.getId()) && StringUtil.isNotBlank(facilityDto.getAccount())
                    && StringUtil.isNotBlank(facilityDto.getPassword()) && StringUtil.isNotBlank(facilityDto.getIp())
                    && (facilityDto.getPort() > 0)) {
                //开启线程池
                Runnable loginRunnable = () -> {
                    //登录设备
                    if (!hikVisionModule.login(facilityDto)) {
                        List<String> failDevice = IConst.failDevice;
                        if (!failDevice.contains(facilityDto.getIp())) {
                            IConst.failDevice.add(facilityDto.getIp());
                        }
                    }
                };
                //提交线程
                ThreadPoolUtil.newHkCachedThreadPool().submit(loginRunnable);

            } else {
                log.warn("设备基础信息异常：->" + facilityDto);
            }

        }
        // Thread.sleep(10000);
        //hikVisionModule.getAllCard(device);
    }
}
