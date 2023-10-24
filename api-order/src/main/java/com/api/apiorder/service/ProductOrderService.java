package com.api.apiorder.service;

import com.api.apicommon.model.entity.Order;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.model.vo.OrderVO;
import com.api.apicommon.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface ProductOrderService extends IService<Order> {
    /**
     * 保存产品订单
     *
     * @param productId 产品id
     * @param loginUser 登录用户
     * @return {@link OrderVO}
     */
    OrderVO saveProductOrder(Long productId, User loginUser);

    /**
     * 更新产品订单
     *
     * @param order 产品订单
     * @return boolean
     */
    boolean updateProductOrder(Order order);

    /**
     * 获取产品订单
     * 获取订单
     *
     * @param productId 产品id
     * @param loginUser 登录用户
     * @param payType   付款类型
     * @return {@link OrderVO}
     */
    OrderVO getProductOrder(Long productId, User loginUser, Integer payType);

    /**
     * 按订单号更新订单状态
     *
     * @param outTradeSn  订单号
     * @param orderStatus 订单状态
     * @return boolean
     */
    boolean updateOrderStatusByOrderNo(String outTradeSn, Integer orderStatus);

    /**
     * 按订单号关闭订单
     *
     * @param outTradeSn 外贸编号
     * @throws Exception 例外
     */
    void closedOrderByOrderNo(String outTradeSn) throws Exception;

    /**
     * 通过outTradeSn获得产品订单
     * 获取产品订单状态
     *
     * @param outTradeSn 外贸编号
     * @return {@link String}
     */
    Order getOrderByOutTradeNo(String outTradeSn);

    /**
     * 付款通知
     * 处理付款通知
     *
     * @param notifyData 通知数据
     * @param request    要求
     * @return {@link String}
     */
    String doPaymentNotify(String notifyData, HttpServletRequest request);
}
