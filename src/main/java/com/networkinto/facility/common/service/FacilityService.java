package com.networkinto.facility.common.service;

import com.networkinto.facility.common.dto.FacilityStatusDto;

/**
 * @author cuiEnMing
 * @date 2021/5/18 9:33
 */
public interface FacilityService {
    /**
     * 验证设备状态
     *
     * @param facilityStatusDto
     */
    void checkFacility(FacilityStatusDto facilityStatusDto);
}
