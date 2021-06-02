package com.networkinto.facility.ahikVision.controller;

import com.google.gson.Gson;
import com.networkinto.facility.ahikVision.service.HikService;
import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.ajHua.utils.JsonResult;
import com.networkinto.facility.common.dto.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.sound.sampled.Line;
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
    @Resource
    private AjHuaService ajHuaService;

    @GetMapping()
    private JsonResult<List<CardDataDto>> queryCard(String serialNumber) {
        return JsonResult.ok("ok", hikService.queryCard(serialNumber));
    }

    @DeleteMapping()
    private JsonResult<String> deleteCard(CardDataDto facilityDto) {
        return JsonResult.ok("ok", hikService.deleteCard(facilityDto));
    }

    /**
     * 海康设备二维码穿透
     */
    @PostMapping()
    private String checkCode(@RequestBody HikQrCodeDto hikQrCodeDto) {
        RemoteCheck jsonResult = new RemoteCheck(Integer.parseInt(hikQrCodeDto.getSerialNo()), "success");
        RemoteCheck1 remoteCheck1 = new RemoteCheck1(jsonResult);
        Gson gson = new Gson();
        String json = gson.toJson(remoteCheck1);
        return json;
    }

}
