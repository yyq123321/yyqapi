package com.api.backend.service.impl;

import com.api.backend.common.ErrorCode;
import com.api.backend.exception.BusinessException;
import com.api.backend.mapper.UserInterfaceInfoMapper;
import com.api.apicommon.model.entity.UserInterfaceInfo;
import com.api.backend.service.UserInterfaceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userInterfaceInfo.getId();
        Long userId = userInterfaceInfo.getUserId();
        Integer leftNum = userInterfaceInfo.getLeftNum();

        // 创建时，所有参数必须非空
        if (add) {
            if (id <= 0 || userId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (leftNum < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于0");
        }
    }

    @Override
    public boolean invokeCnt(Long interfaceInfoId, Long userId) {
        if(interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数有误");
        }

        //todo 分布式锁

        // 乐观锁
        boolean result = false;
        int maxTry = 5;
        int currentTry = 0;
        do {
            // 查询用户接口信息
            LambdaQueryWrapper<UserInterfaceInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                    .eq(UserInterfaceInfo::getUserId, userId);
            UserInterfaceInfo userInterfaceInfo = getOne(queryWrapper);

            // 更新操作
            userInterfaceInfo.setTotalNum(userInterfaceInfo.getTotalNum() + 1);
            userInterfaceInfo.setLeftNum(userInterfaceInfo.getLeftNum() - 1);
            userInterfaceInfo.setVersion(userInterfaceInfo.getVersion() + 1);

            // 尝试更新
            LambdaUpdateWrapper<UserInterfaceInfo> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                    .eq(UserInterfaceInfo::getUserId, userId)
                    .eq(UserInterfaceInfo::getVersion, userInterfaceInfo.getVersion() - 1); // 使用之前的版本号进行比较
            result = update(userInterfaceInfo, updateWrapper);

            if (result) {
                break; // 更新成功，退出循环
            } else {
                // 更新失败，重试更新
                currentTry ++;
            }
        } while (currentTry < maxTry);

        return result;
    }

    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {
        return false;
    }

    @Override
    public int getLeftInvokeCount(long userId, long interfaceInfoId) {
        return 0;
    }
}
