package com.networkinto.facility.scheduled;

import com.networkinto.facility.ahikVision.module.HikVisionModule;
import com.networkinto.facility.ahikVision.service.HikService;
import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.PingUtils;
import com.networkinto.facility.common.constant.UrlUtils;
import com.networkinto.facility.common.dto.FacilityDto;
import com.networkinto.facility.common.dto.HumanFaceDto;
import com.networkinto.facility.opencv.OpencvFace;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author cuiEnMing
 * 定时任务
 */
@Log4j2
@Component
@EnableScheduling
public class FacilityJob {
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private AjHuaService ajHuaService;
    @Resource
    private HikService hikService;
    @Resource
    private HikVisionModule hikVisionModule;

    /**
     * 人脸下发
     */
    // 0 */2 * * * ?
    // @Scheduled(cron = "*/5 * * * * ?")
    private void humanFace() {
        //url前半段相同
        String wisdomCommunityUrl = UrlUtils.wisdomCommunityUrl();
        //查询需下发的任务
        String faceUrl = wisdomCommunityUrl + IConst.wisdomCommunity.FACE_INTERFACE.getName();
        //向智慧小区返回下发结果
        String resultUrl = wisdomCommunityUrl + IConst.wisdomCommunity.FACE_RESULT.getName();
        //过滤注册失败的设备 不做该设备下发
        List<String> failDevice = IConst.failDevice;
        //返回数据下发结果
        List<HumanFaceDto> faceResult = new ArrayList<>();
        ParameterizedTypeReference<List<HumanFaceDto>> type = new ParameterizedTypeReference<List<HumanFaceDto>>() {
        };
        log.info("拼接url为:->" + faceUrl);
        ResponseEntity<List<HumanFaceDto>> responseEntity = restTemplate.exchange(faceUrl, HttpMethod.GET, null, type);
        List<HumanFaceDto> body = responseEntity.getBody();
        //数据为空return
        if (null == body || IConst.SUCCEED == body.size()) {
            return;
        }
        for (HumanFaceDto faceDto : body) {
            if (failDevice.contains(faceDto.getIp())) {
                log.info("跳过设备 ip：" + faceDto.getIp());
                continue;
            }
            if (faceDto.getNoNum() > 3) {
                log.info("失败次数大于三次 不在继续下发 用户名：" + faceDto.getPersonName());
                continue;
            }
            String imgUrl = faceDto.getFaceUrl();
            if (IConst.SUCCEED == imgUrl.length() || null == faceDto.getCardNo()) {
                continue;
            }
            try {

                String url = "https://face.techzhl.com/face/crop_face?face_url=";
                String uerEnd = "&type=base64";
                String allUrl = url + imgUrl + uerEnd;
                log.info("拼接url路径为->{}", allUrl);
                String forObject = restTemplate.getForObject(allUrl, String.class);
                String[] split = forObject.split(",");
                String s = split[1];
                byte[] bytes = Base64.getDecoder().decode(s);
               /* byte[] bytes = UrlUtils.imgConvert(imgUrl);
               // int i = bytes.length / 1024;
              if (bytes.length  / 1024>IConst.IMG_SIZE){

                }*/
               /* Mat mat = UrlUtils.inputStream2Mat(imgUrl);
                byte[] bytes = opencvFace.buttonFace(mat);*/
                HumanFaceDto humanFaceDto;
                if (faceDto.getDeviceType() == 2) {
                    humanFaceDto = ajHuaService.addPicture(bytes, faceDto.getPersonName(), faceDto);
                } else {
                    humanFaceDto = hikService.addCard(faceDto);
                    // 卡状态==-1 代表卡号下发失败
                    if (humanFaceDto.getCardStatus() != -1) {
                        humanFaceDto = hikService.addPicture(bytes, faceDto);
                    }
                }
                faceResult.add(humanFaceDto);
            } catch (Exception ioException) {
                ioException.printStackTrace();
            }
        }
        if (faceResult.size() > IConst.SUCCEED) {
            restTemplate.postForEntity(resultUrl, faceResult, String.class);
        }
    }

    /**
     * 设备重连
     */
    //  @Scheduled(cron = "0 */10 * * * ?")
   // @Scheduled(cron = "*/5 * * * * ?")
    private void reconnect() {
        List<FacilityDto> facilityDtoList = hikVisionModule.getFacilityDtoList();
        log.info("设备重连启动 -> list长度{}", facilityDtoList.size());
        if (facilityDtoList.size() > IConst.SUCCEED) {
            for (int i = 0; i < facilityDtoList.size(); i++) {
                FacilityDto facilityDto = facilityDtoList.get(i);
                facilityDto.setDeviceType(IConst.IMG_SIZE);
                if (PingUtils.ping(facilityDto.getIp(), facilityDto.getPort(), 3000)) {
                    if (hikVisionModule.login(facilityDto)) {
                        hikVisionModule.qrCode(facilityDto.getSerialNumber());
                        facilityDtoList.remove(i);
                        log.info("移除登录成功的设备");
                    }
                } else {
                    log.info("设备重连失败");
                }
            }
        }
    }
}
