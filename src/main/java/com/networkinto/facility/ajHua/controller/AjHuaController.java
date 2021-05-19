package com.networkinto.facility.ajHua.controller;

import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.ajHua.utils.JsonResult;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.InterfaceReturnsDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author cuiEnMing
 * @Desc 为第三方提供的调用接口
 */
@Log4j2
@RestController
@RequestMapping("/aj/hua")
public class AjHuaController {
    @Resource
    private AjHuaService ajHuaService;
    /**
     * @return JsonResult
     * @Description 查询卡信息
     * @params deviceDto 设备
     * @author cuiEnMing
     */
    @GetMapping("/query/card")
    private List<CardDataDto> queryCard(@RequestParam String serialNumber) {
        return ajHuaService.queryCard(serialNumber);
    }
    /**
     * @return JsonResult
     * @Description 删除卡信息
     * @params deviceDto 设备
     * @author cuiEnMing
     */
    @DeleteMapping("/delete/card")
    private JsonResult<String> deleteCard(@RequestBody CardDataDto cardDataDto) {
        return ajHuaService.deleteCard(cardDataDto);
    }

    /**
     * @param code 二维码字符串
     * @return JsonResult
     * @Description 验证二维码
     * @params serial 设备序列号
     * @author cuiEnMing
     */
    @PostMapping("/{serial}/QR_CODE")
    private JsonResult<String> checkQrCode(@RequestParam String code, @PathVariable String serial) {
        return ajHuaService.checkQrCode(code);
    }

    /**
     * @return JsonResult
     * @Description 关闭二维码
     * @params deviceDto 设备
     * @author cuiEnMing
     */
    @PostMapping("/close/QR_CODE")
    private JsonResult<String> closeQrCOde(@RequestParam FacilityDto facilityDto) {
        InterfaceReturnsDto returnsDto = ajHuaService.closeQrCOde(facilityDto);
        if (IConst.SUCCEED.equals(returnsDto.getCode())) {
            return JsonResult.ok(returnsDto.getMessage(), returnsDto.getData());
        }
        return JsonResult.error(returnsDto.getMessage(), returnsDto.getData(), returnsDto.getCode());
    }


    /**
     * @return JsonResult
     * @Description 设备登出
     * @params deviceDto 设备
     * @author cuiEnMing
     */
    @PostMapping("/device/out")
    private JsonResult<String> outDevice(@RequestParam String deviceDto) {

        return ajHuaService.outDevice(deviceDto);
    }
}
