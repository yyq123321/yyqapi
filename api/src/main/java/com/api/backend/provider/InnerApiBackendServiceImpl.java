package com.api.backend.provider;

import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.model.entity.InterfaceInfo;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.service.ApiBackendService;
import com.api.backend.mapper.InterfaceInfoMapper;
import com.api.backend.mapper.UserInterfaceInfoMapper;
import com.api.backend.mapper.UserMapper;
import com.api.backend.service.UserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@DubboService
public class InnerApiBackendServiceImpl implements ApiBackendService {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

//    @Resource
//    private InterfaceChargingService interfaceChargingService;


    @Override
    public User getInvokeUser(String accessKey) {

        if (StringUtils.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey",accessKey);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public InterfaceInfo getInterFaceInfo(String url, String method) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(method)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url",url);
        queryWrapper.eq("method",method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {
        return userInterfaceInfoService.invokeCount(userId,interfaceInfoId);
    }

    @Override
    public int getLeftInvokeCount(long userId, long interfaceInfoId) {
        return userInterfaceInfoService.getLeftInvokeCount(userId,interfaceInfoId);
    }

    @Override
    public InterfaceInfo getInterfaceById(long interfaceId) {
        return interfaceInfoMapper.selectById(interfaceId);
    }

    @Override
    public int getInterfaceStockById(long interfaceId) {
//        QueryWrapper<InterfaceCharging> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("interfaceId",interfaceId);
//        InterfaceCharging interfaceCharging = interfaceChargingService.getOne(queryWrapper);
//        if (interfaceCharging == null){
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口不存在");
//        }
//        return Integer.parseInt(interfaceCharging.getAvailablePieces());
        return 1;
    }

    @Override
    public boolean updateInterfaceStock(long interfaceId,Integer num) {
//        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.setSql("availablePieces = availablePieces - "+num)
//                .eq("interfaceId",interfaceId).gt("availablePieces",num);
//
//        return interfaceChargingService.update(updateWrapper);
        return true;
    }

    @Override
    public boolean recoverInterfaceStock(long interfaceId, Integer num) {
//        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
//        updateWrapper.setSql("availablePieces = availablePieces + "+num)
//                .eq("interfaceId",interfaceId);
//        return interfaceChargingService.update(updateWrapper);
        return true;
    }

    @Override
    public boolean updateUserInterfaceInvokeCount(long userId, long interfaceId, int num) {
//        UpdateUserInterfaceInfoDTO userInterfaceInfoDTO = new UpdateUserInterfaceInfoDTO();
//        userInterfaceInfoDTO.setUserId(userId);
//        userInterfaceInfoDTO.setInterfaceId(interfaceId);
//        userInterfaceInfoDTO.setLockNum((long)num);
//        return userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoDTO);
        return true;
    }
}
