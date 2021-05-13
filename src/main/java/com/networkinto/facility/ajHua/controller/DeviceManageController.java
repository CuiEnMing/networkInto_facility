package com.networkinto.facility.ajHua.controller;

import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.ajHua.utils.JsonResult;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.dto.CardDataDto;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.HumanFaceDto;
import com.networkinto.facility.common.dto.InterfaceReturnsDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author cuiEnMing
 * @Desc 为第三方提供的调用接口
 */
@Log4j2
@RestController
@RequestMapping("/device/manage")
public class DeviceManageController {
    @Resource
    private AjHuaService ajHuaService;

    /**
     * @param file 图片
     * @return JsonResult
     * @author cuiEnMing
     */
    @PostMapping
    private JsonResult<List<Map<String, String>>> insertUser(@RequestBody() List<MultipartFile> file, @RequestBody List<HumanFaceDto> humanFaceDtos) {
        List<Map<String, String>> maps = ajHuaService.addFace(file, humanFaceDtos);
        if (!maps.isEmpty()) {
            return JsonResult.ok("存在下发失败数据 详情查看data", maps);
        }
        return JsonResult.okNoData("人脸下发成功");
    }

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
     * @Description 修改卡信息
     * @params deviceDto 设备
     * @author cuiEnMing
     */
    @PutMapping("/update/card")
    private JsonResult<String> updateCard(@RequestBody CardDataDto cardDataDto) {
        return ajHuaService.updateCard(cardDataDto);
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
        if (IConst.SUCCEED_CODE.equals(returnsDto.getCode())) {
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
