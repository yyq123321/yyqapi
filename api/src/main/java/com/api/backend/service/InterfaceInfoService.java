package com.api.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.api.apicommon.model.entity.InterfaceInfo;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);


}
