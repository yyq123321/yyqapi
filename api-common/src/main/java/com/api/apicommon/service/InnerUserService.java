package com.api.apicommon.service;

import com.api.apicommon.model.entity.User;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface InnerUserService {

    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

    boolean updateUserBalance(Long userId, Long addBalance);

    boolean recoverUserBalance(Long userId, Long addBalance);

    boolean invokeCount(Long userId, Long price);


}
