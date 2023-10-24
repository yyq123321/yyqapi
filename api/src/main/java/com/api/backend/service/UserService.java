package com.api.backend.service;

import com.api.backend.common.BaseResponse;
import com.api.backend.model.vo.LoginUserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.api.apicommon.model.entity.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param phone
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String phone);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request, HttpServletResponse response);


    void sendCode(String email, String captchaType);

    BaseResponse<Integer> changeStatus(Integer status, Long userId, HttpServletRequest request);
}
