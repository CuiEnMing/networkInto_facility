package com.networkinto.facility.scheduled;
import com.networkinto.facility.ahikVision.service.HikService;
import com.networkinto.facility.ajHua.service.AjHuaService;
import com.networkinto.facility.common.constant.IConst;
import com.networkinto.facility.common.dto.HumanFaceDto;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    /**
     * 人脸下发
     */
    @Scheduled(cron = "*/5 * * * * ?")
    private void humanFace() {
        //过滤注册失败的设备 不做该设备下发
        List<String> failDevice = new ArrayList<>();
        //返回数据下发结果
        List<HumanFaceDto> faceResult = new ArrayList<>();
        ParameterizedTypeReference<List<HumanFaceDto>> type = new ParameterizedTypeReference<List<HumanFaceDto>>() {
        };
        ResponseEntity<List<HumanFaceDto>> responseEntity = restTemplate.exchange(IConst.FACE_DATA, HttpMethod.GET, null, type);
        List<HumanFaceDto> body = responseEntity.getBody();
        //数据为空直return
        if (IConst.SUCCEED_CODE == body.size()) {
            return;
        }
        //根据设备序列号分组 方便批量导入
        Map<String, List<HumanFaceDto>> listMap = body.stream().collect(Collectors.groupingBy(HumanFaceDto::getSerialNumber));
        listMap.forEach((key, val) -> {
            for (HumanFaceDto dto : val) {
                //过滤异常设备任务
                if (failDevice.contains(dto.getIp())) {
                    log.info("跳过设备 ip：" + dto.getIp());
                    continue;
                }
                if (dto.getNoNum() > 3) {
                    log.info("失败次数大于三次 不在继续下发 用户名：" + dto.getPersonName());
                    continue;
                }
            }
        });
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
            if (IConst.SUCCEED_CODE == imgUrl.length() || null == faceDto.getCardNo()) {
                continue;
            }
            try {
                byte[] bytes = IConst.imgConvert(imgUrl);
                HumanFaceDto humanFaceDto = new HumanFaceDto();
                if (faceDto.getDeviceType() == 0) {
                    humanFaceDto = ajHuaService.addPicture(bytes, faceDto.getPersonName(), faceDto);
                } else {
                    humanFaceDto = hikService.addCard(faceDto);
                    /**
                     * 卡状态==-1 代表卡号下发失败
                     * */
                    if (humanFaceDto.getCardStatus() != -1) {
                        humanFaceDto = hikService.addPicture(bytes, faceDto);
                    }
                }
                faceResult.add(humanFaceDto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (faceResult.size() > IConst.SUCCEED_CODE) {
            restTemplate.postForEntity(IConst.FACE_RESULT, faceResult, String.class);
        }
    }
}
