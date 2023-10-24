package com.api.backend.service;

import com.api.apicommon.model.entity.UserInterfaceInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b);

    boolean invokeCnt(Long interfaceInfoId, Long userId);

    boolean invokeCount(long userId, long interfaceInfoId);

    int getLeftInvokeCount(long userId, long interfaceInfoId);
}
