package com.api.apiorder.service;

import com.api.apicommon.model.entity.Order;
import com.api.apicommon.model.vo.OrderVO;
import com.api.apiorder.model.dto.OrderCreateRequest;
import com.api.apiorder.model.dto.OrderQueryRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * @param orderCreateRequest
     * @param request
     * @return
     */
    OrderVO createOrder(OrderCreateRequest orderCreateRequest, HttpServletRequest request);

    /**
     * 获取我的订单
     * @param orderQueryRequest
     * @param request
     * @return
     */
    Page<OrderVO> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request);

    OrderVO getOrder(String orderSn, HttpServletRequest request);

    Boolean cancelOrder(String orderSn, HttpServletRequest request);

    String callbackOrder(String notifyData, HttpServletRequest request);
}
