package com.api.backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.api.apicommon.common.JwtUtils;
import com.api.apicommon.model.entity.SmsMessage;
import com.api.backend.common.BaseResponse;
import com.api.backend.common.ErrorCode;
import com.api.backend.common.ResultUtils;
import com.api.backend.exception.BusinessException;
import com.api.backend.model.vo.LoginUserVO;
import com.api.backend.service.UserService;
import com.api.backend.utils.LeakyBucket;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.api.apicommon.model.entity.User;
import com.api.backend.mapper.UserMapper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.api.apicommon.constant.RabbitmqConstant.EXCHANGE_SMS_INFORM;
import static com.api.apicommon.constant.RabbitmqConstant.ROUTINGKEY_SMS;
import static com.api.backend.constant.UserConstant.USER_LOGIN_STATE;
import static com.api.backend.utils.LeakyBucket.loginLeakyBucket;
import static com.api.backend.utils.LeakyBucket.registerLeakyBucket;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private Gson gson;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "api";

    //登录和注册的标识，方便切换不同的令牌桶来限制验证码发送
    private static final String LOGIN_SIGN = "login";

    private static final String REGISTER_SIGN = "register";

    public static final String USER_LOGIN_EMAIL_CODE = "user:login:email:code:";
    public static final String USER_REGISTER_EMAIL_CODE = "user:register:email:code:";

    /**
     * 图片验证码 redis 前缀
     */
    private static final String CAPTCHA_PREFIX = "api:captchaId:";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String phone) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        if (!Pattern.matches("^1[3-9]\\d{9}$", phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不对");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 手机号不能重复
            QueryWrapper<User> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("phone", phone);
            count = userMapper.selectCount(queryWrapper2);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号已经注册过了");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配 accessKey, secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setPhone(phone);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request, HttpServletResponse response) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 记录用户的登录态
        return setLoginUser(response, user);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Long userId = JwtUtils.getUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }

        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_STATE + userId);
        User user = gson.fromJson(userJson, User.class);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }
        User currentUser = getById(userId);
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        User user = getLoginUser(request);
        return isAdmin(user);
    }


    public boolean isAdmin(User user) {
        return user != null && com.api.backend.model.enums.UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = new Cookie[0];
        try {
            cookies = request.getCookies();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                Long userId = JwtUtils.getUserIdByToken(request);
                stringRedisTemplate.delete(USER_LOGIN_STATE + userId);
                Cookie timeOutCookie = new Cookie(cookie.getName(), cookie.getValue());
                timeOutCookie.setMaxAge(0);
                response.addCookie(timeOutCookie);
                return true;
            }
        }
        return false;
    }

    private LoginUserVO setLoginUser(HttpServletResponse response, User user) {
        String token = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        Cookie cookie = new Cookie("token", token);
        cookie.setPath("/");
        cookie.setDomain("yyq-api.yyq-personal-code.cn");//需要设置为同一个域名使得cookie能够发送
        response.addCookie(cookie);
        String userJson = gson.toJson(user);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_STATE + user.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.getLoginUserVO(user);
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public void sendCode(String email, String captchaType) {


        if (StringUtils.isBlank(captchaType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码类型为空!!!");
        }

        //令牌桶算法实现短信接口的限流，因为手机号码重复发送短信，要进行流量控制
        //解决同一个手机号的并发问题，锁的粒度非常小，不影响性能。只是为了防止用户第一次发送短信时的恶意调用
        synchronized (email.intern()) {
            Boolean exist = stringRedisTemplate.hasKey(USER_LOGIN_EMAIL_CODE + email);
            if (exist != null && exist) {
                //1.令牌桶算法对手机短信接口进行限流 具体限流规则为同一个手机号，60s只能发送一次
                long lastTime = 0L;
                LeakyBucket leakyBucket = null;
                if (captchaType.equals(REGISTER_SIGN)) {
                    String strLastTime = stringRedisTemplate.opsForValue().get(USER_REGISTER_EMAIL_CODE + email);
                    if (strLastTime != null) {
                        lastTime = Long.parseLong(strLastTime);
                    }
                    leakyBucket = registerLeakyBucket;
                } else {
                    String strLastTime = stringRedisTemplate.opsForValue().get(USER_LOGIN_EMAIL_CODE + email);
                    if (strLastTime != null) {
                        lastTime = Long.parseLong(strLastTime);
                    }
                    leakyBucket = loginLeakyBucket;
                }

                if (!leakyBucket.control(lastTime)) {
                    log.info("邮箱发送太频繁了");
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱送太频繁了");
                }
            }

            //2.符合限流规则则生成手机短信
            String code = RandomUtil.randomNumbers(4);
            SmsMessage smsMessage = new SmsMessage(email, code);


            //消息队列异步发送短信，提高短信的吞吐量
            rabbitTemplate.convertAndSend(EXCHANGE_SMS_INFORM, ROUTINGKEY_SMS, smsMessage);

            log.info("邮箱对象：" + smsMessage.toString());
            //更新手机号发送短信的时间
            if (captchaType.equals(REGISTER_SIGN)) {
                stringRedisTemplate.opsForValue().set(USER_REGISTER_EMAIL_CODE + email, "" + System.currentTimeMillis() / 1000);
            } else {
                stringRedisTemplate.opsForValue().set(USER_LOGIN_EMAIL_CODE + email, "" + System.currentTimeMillis() / 1000);
            }

        }

    }

    @Override
    public BaseResponse<Integer> changeStatus(Integer status, Long userId, HttpServletRequest request) {
        if (userId == null || userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "学生id有误");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getStatus, status);
        boolean isSuccess = false;
        try {
            isSuccess = update(wrapper);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据库操作异常");
        }
        if (isSuccess) {
            return ResultUtils.success(1);
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "参数有误");
        }
    }

}




