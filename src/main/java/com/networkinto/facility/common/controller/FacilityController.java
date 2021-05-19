package com.networkinto.facility.common.controller;

import com.networkinto.facility.common.dto.FacilityStatusDto;
import com.networkinto.facility.common.service.FacilityService;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 检测设备状态
 *
 * @author cuiEnMing
 * @date 2021/5/18 9:01
 */
@RestController
@RequestMapping("/facility")
public class FacilityController {
    @Resource
    private FacilityService facilityService;

    @ApiOperation(value = "设备状态检测")
    @PostMapping ("/status")
    public void facilityStatus(@RequestBody  @Validated  FacilityStatusDto facilityStatusDto) {
        facilityService.checkFacility(facilityStatusDto);
    }

}
