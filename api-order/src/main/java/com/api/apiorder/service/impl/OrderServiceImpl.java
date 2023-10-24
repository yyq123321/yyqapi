package com.api.apiorder.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.api.apicommon.model.entity.Order;
import com.api.apicommon.model.entity.ProductInfo;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.model.vo.OrderVO;
import com.api.apicommon.service.InnerProductInfoService;
import com.api.apiorder.enums.OrderStatusEnum;
import com.api.apiorder.mapper.OrderMapper;
import com.api.apiorder.model.dto.OrderCreateRequest;
import com.api.apiorder.model.dto.OrderQueryRequest;
import com.api.apiorder.service.OrderService;
import com.api.apiorder.service.ProductOrderService;
import com.api.apiorder.utils.OrderMqUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.common.JwtUtils;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.service.ApiBackendService;
import com.api.apicommon.service.InnerUserService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order>
        implements OrderService {


    @DubboReference
    private InnerProductInfoService innerProductInfoService;

    @DubboReference
    private InnerUserService userService;


    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private Gson gson;

    @Resource
    private ProductOrderService productOrderService;

    public static final String USER_LOGIN_STATE = "user_login";

    /**
     * 创建订单
     *
     * @param orderCreateRequest
     * @param request
     * @return
     */
    @Transactional
    @Override
    public OrderVO createOrder(OrderCreateRequest orderCreateRequest, HttpServletRequest request) {

        // 1.订单服务校验参数，如用户是否存在，接口是否存在等校验

        if (orderCreateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = orderCreateRequest.getUserId();
        Long productId = orderCreateRequest.getProductId();

        if (userId == null || productId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        // 校验商品
        ProductInfo productInfo = innerProductInfoService.getProductInfoById(productId);
        if (productInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品不存在");
        }

        // 后端校验订单总价格
        Long total = productInfo.getTotal();
        // 虚拟货币
        Long addCoins = productInfo.getAddCoins();

        // 2.数据库保存订单数据, 方法内有消息队列消息发送
        OrderVO orderVO = productOrderService.saveProductOrder(productId, loginUser);

        return orderVO;
    }

    /**
     * 分页查询订单
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<OrderVO> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {

        long current = orderQueryRequest.getCurrent();
        long pageSize = orderQueryRequest.getPageSize();

        // 限制爬虫
        if (pageSize > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User userVO = getLoginUser(request);
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userVO.getId());
        // 未支付的订单前置
        queryWrapper.last("ORDER BY CASE WHEN status = 0 THEN 0 ELSE 1 END, status");
        Page<Order> page = new Page<>(current, pageSize);
        Page<Order> orderPage = this.page(page, queryWrapper);

        Page<OrderVO> orderVOPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());

        List<OrderVO> orderVOList = orderPage.getRecords().stream().map(order -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setTotalAmount(order.getPayAmount());
            orderVO.setOrderNumber(order.getOrderSn());
            return orderVO;
        }).collect(Collectors.toList());
        orderVOPage.setRecords(orderVOList);

        return orderVOPage;

    }

    @Override
    public OrderVO getOrder(String orderSn, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, userId);
        Order order = getOne(wrapper);

        //拷贝
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setTotalAmount(order.getPayAmount());
        ProductInfo productInfo = innerProductInfoService.getProductInfoById(order.getProductId());
        orderVO.setProductInfo(productInfo);
        return orderVO;
    }

    @Override
    public Boolean cancelOrder(String orderSn, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        Long userId = loginUser.getId();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderSn, orderSn)
                .eq(Order::getUserId, userId)
                .set(Order::getStatus, 2);   //消息队列的订单延时自动删除
        boolean result = update(wrapper);
        return result;
    }

    @Override
    public String callbackOrder(String notifyData, HttpServletRequest request) {
        return productOrderService.doPaymentNotify(notifyData, request);
    }

    /**
     * 生成订单号
     *
     * @return
     */
    private String generateOrderNum(Long userId) {
        String timeId = IdWorker.getTimeId();
        String substring = timeId.substring(0, timeId.length() - 15);
        return substring + RandomUtil.randomNumbers(5) + userId;
    }

    /**
     * 获取登录用户
     *
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Long userId = JwtUtils.getUserIdByToken(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_STATE + userId);
        User user = gson.fromJson(userJson, User.class);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return user;
    }

}




