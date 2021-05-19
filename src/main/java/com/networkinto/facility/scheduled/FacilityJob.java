package com.networkinto.facility.scheduled;

import com.networkinto.facility.ahikVision.service.HikService;
import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.constant.UrlUtils;
import com.networkinto.facility.common.dto.HumanFaceDto;
import com.networkinto.facility.opencv.OpencvFace;
import lombok.extern.log4j.Log4j2;
import org.opencv.core.Mat;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cuiEnMing
 * @Desc 定时任务
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
    private OpencvFace opencvFace;

    /**
     * 人脸下发
     */
     @Scheduled(cron = "*/5 * * * * ?")
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
        //数据为空直return
        if (IConst.SUCCEED == body.size()) {
            return;
        }
        //根据设备序列号分组 方便批量导入
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
                Mat mat = UrlUtils.inputStream2Mat(imgUrl);
                byte[] bytes = opencvFace.buttonFace(mat);
                HumanFaceDto humanFaceDto;
                if (faceDto.getDeviceType() == 0) {
                    humanFaceDto = ajHuaService.addPicture(bytes, faceDto.getPersonName(), faceDto);
                } else {
                    humanFaceDto = hikService.addCard(faceDto);
                    /**
                     * 卡状态==-1 代表卡号下发失败
                     * */
                    if (true) {
                        humanFaceDto = hikService.addPicture(bytes, faceDto);
                    }
                }
                faceResult.add(humanFaceDto);
            } catch (IOException ioException) {
                log.error("图片转换错误 路径->：" + imgUrl);
                faceDto.setNoNum(4);
                faceDto.setCardStatus(-1);
                faceDto.setFingerStatus(-1);
                faceDto.setFailRemark("图片转换错误 路径->：" + imgUrl);
                faceResult.add(faceDto);
                log.info("返回人脸下发结果url:->" + resultUrl);
                restTemplate.postForEntity(resultUrl, faceResult, String.class);
                ioException.printStackTrace();
            } catch (Exception e) {
                log.error("格式转换异常->：" + imgUrl);
                e.printStackTrace();
            }
        }
        if (faceResult.size() > IConst.SUCCEED) {
            restTemplate.postForEntity(resultUrl, faceResult, String.class);
        }
    }
}
