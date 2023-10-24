package com.api.backend.provider;

import com.api.apicommon.model.entity.User;
import com.api.apicommon.service.InnerUserService;
import com.api.backend.common.ErrorCode;
import com.api.backend.exception.BusinessException;
import com.api.backend.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserMapper userMapper;


    @Override
    public User getInvokeUser(String accessKey) {
        if(StringUtils.isAnyBlank(accessKey)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey", accessKey);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateUserBalance(Long userId, Long addBalance) {
        // 根据 userId 查询出原有对象
        User user = userMapper.selectById(userId);

        if (user != null) {
            // 在原值上加上 addBalance
            Long newBalance = user.getBalance() + addBalance;
            // 更新对象的 balance 值
            user.setBalance(newBalance);
            // 执行更新操作
            int affectedRows = userMapper.updateById(user);
            return affectedRows > 0; // 根据实际需求返回更新结果
        } else {
            return false; // 如果用户不存在，返回更新失败
        }
    }

    @Override
    public boolean recoverUserBalance(Long userId, Long addBalance) {
        // 根据 userId 查询出原有对象
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 在原值上减去 addBalance
            Long newBalance = null;
            try {
                newBalance = user.getBalance() - addBalance;
                // 更新对象的 balance 值
                user.setBalance(newBalance);
                // 执行更新操作
                int affectedRows = userMapper.updateById(user);
                return affectedRows > 0; // 根据实际需求返回更新结果
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "回滚异常");
            }
        } else {
            return false; // 如果用户不存在，返回更新失败
        }
    }

    @Override
    public boolean invokeCount(Long userId, Long price) {
        // 根据 userId 查询出原有对象
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 在原值上减去 addBalance
            Long newBalance = null;
            try {
                newBalance = user.getBalance() - price;
                // 更新对象的 balance 值
                user.setBalance(newBalance);
                // 执行更新操作
                int affectedRows = userMapper.updateById(user);
                return affectedRows > 0; // 根据实际需求返回更新结果
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "扣减货币异常");
            }
        } else {
            return false; // 如果用户不存在，返回更新失败
        }
    }
}
