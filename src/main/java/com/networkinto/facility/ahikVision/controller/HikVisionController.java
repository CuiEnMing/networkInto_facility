package com.networkinto.facility.ahikVision.controller;

import com.networkinto.facility.ahikVision.service.HikService;
import com.networkinto.facility.ajHua.utils.JsonResult;
import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 海康设备controller
 *
 * @author cuiEnMing
 * @date 2021/5/14 9:52
 */
@Log4j2
@RestController
@RequestMapping("/hik/vision")
public class HikVisionController {
    @Resource
    private HikService hikService;

    @GetMapping()
    private JsonResult<List<CardDataDto>> queryCard() {
        FacilityDto facilityDto = new FacilityDto();
        return JsonResult.ok("ok", hikService.queryCard(facilityDto));
    }
    @DeleteMapping()
    private JsonResult<String> deleteCard(CardDataDto facilityDto) {
        return JsonResult.ok("ok", hikService.deleteCard(facilityDto));
    }

}
