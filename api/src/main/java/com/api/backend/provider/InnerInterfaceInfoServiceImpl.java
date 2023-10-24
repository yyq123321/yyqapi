package com.api.backend.provider;

import com.api.apiclientsdk.client.ApiClient;
import com.api.apicommon.model.entity.InterfaceInfo;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.service.InnerInterfaceInfoService;
import com.api.backend.common.ErrorCode;
import com.api.backend.common.ResultUtils;
import com.api.backend.exception.BusinessException;
import com.api.backend.mapper.InterfaceInfoMapper;
import com.api.backend.model.enums.InterfaceInfoStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {
    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public InterfaceInfo getInterfaceInfo(String url, String method) {
        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url);
        queryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

}
