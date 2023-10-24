package com.api.apiorder.controller;


import com.api.apicommon.model.vo.OrderVO;
import com.api.apiorder.model.dto.OrderCreateRequest;
import com.api.apiorder.model.dto.OrderQueryRequest;
import com.api.apiorder.service.OrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.api.apicommon.common.BaseResponse;
import com.api.apicommon.common.ResultUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;


    /**
     * 创建订单
     *
     * @param orderCreateRequest
     * @param request
     * @return
     */
    @PostMapping("/create")
    public BaseResponse<OrderVO> createOrder(@RequestBody OrderCreateRequest orderCreateRequest, HttpServletRequest request) {
        OrderVO order = orderService.createOrder(orderCreateRequest, request);
        return ResultUtils.success(order);
    }

    /**
     * 获取订单
     *
     * @param orderSn
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<OrderVO> getOrder(@RequestParam("orderSn") String orderSn, HttpServletRequest request) {
        OrderVO order = orderService.getOrder(orderSn, request);
        return ResultUtils.success(order);
    }

    /**
     * 支付结果回调
     *
     * @param notifyData
     * @param request
     * @return
     */
    @PostMapping("/pay/notify")
    public String callbackOrder(@RequestBody String notifyData, HttpServletRequest request) {
        return orderService.callbackOrder(notifyData, request);
    }

    /**
     * 取消订单
     *
     * @param orderSn
     * @param request
     * @return
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelOrder(String orderSn, HttpServletRequest request) {
        Boolean result = orderService.cancelOrder(orderSn, request);
        return ResultUtils.success(result);
    }


    /**
     * 分页查询
     *
     * @param orderQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<Page<OrderVO>> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request) {
        Page<OrderVO> orderPage = orderService.listPageOrder(orderQueryRequest, request);
        return ResultUtils.success(orderPage);
    }


}
