package com.networkinto.facility.common.controller;

import com.networkinto.facility.common.dto.FacilityStatusDto;
import com.networkinto.facility.common.dto.HikQrCodeDto;
import com.networkinto.facility.common.service.FacilityService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 检测设备状态
 *
 * @author cuiEnMing
 * @date 2021/5/18 9:01
 */
@Log4j2
@RestController
@RequestMapping("/facility")
public class FacilityController {
    @Resource
    private FacilityService facilityService;

    @ApiOperation(value = "设备状态检测")
    @PostMapping("/status")
    public void facilityStatus(@RequestBody @Validated FacilityStatusDto facilityStatusDto) {
        facilityService.checkFacility(facilityStatusDto);
    }

    @PostMapping("/code/result")
    public void qrCodeResult( HikQrCodeDto hikQrCodeDto) {
        log.info("code/result 被调用啦 参数->{}", hikQrCodeDto);
    }
}
