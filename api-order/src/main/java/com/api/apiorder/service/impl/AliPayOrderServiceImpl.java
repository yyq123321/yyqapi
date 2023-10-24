package com.api.apiorder.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.model.entity.Order;
import com.api.apicommon.model.entity.ProductInfo;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.model.vo.OrderVO;
import com.api.apicommon.service.InnerProductInfoService;
import com.api.apicommon.service.InnerUserService;
import com.api.apiorder.config.AliPayAccountConfig;
import com.api.apiorder.mapper.OrderMapper;
import com.api.apiorder.model.alipay.AliPayAsyncResponse;
import com.api.apiorder.model.vo.PaymentInfoVO;
import com.api.apiorder.service.PaymentInfoService;
import com.api.apiorder.service.ProductOrderService;
import com.api.apiorder.utils.OrderMqUtils;
import com.api.apiorder.utils.RedissonLockUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.alipay.AliPayApiConfigKit;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.api.apicommon.constant.PayConstant.ORDER_PREFIX;
import static com.api.apiorder.model.enums.AlipayTradeStatusEnum.TRADE_SUCCESS;
import static com.api.apiorder.model.enums.PayTypeStatusEnum.ALIPAY;
import static com.api.apiorder.model.enums.PaymentStatusEnum.*;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Service
@Slf4j
@Qualifier("ALIPAY")
public class AliPayOrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements ProductOrderService {
    @Resource
    private AliPayAccountConfig aliPayAccountConfig;
    @DubboReference
    private InnerUserService userService;
    @DubboReference
    private InnerProductInfoService productInfoService;
    @Resource
    private PaymentInfoService paymentInfoService;
    @Resource
    private OrderMqUtils orderMqUtils;

    @Resource
    private RedissonLockUtil redissonLockUtil;

    /**
     * 获取商品订单
     *
     * @param productId 产品id
     * @param loginUser 登录用户
     * @param payType   付款类型
     * @return
     */
    @Override
    public OrderVO getProductOrder(Long productId, User loginUser, Integer payType) {
        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Order::getProductId, productId);
        lambdaQueryWrapper.eq(Order::getStatus, NOTPAY.getValue());
        lambdaQueryWrapper.eq(Order::getPayType, payType);
        lambdaQueryWrapper.eq(Order::getUserId, loginUser.getId());
        Order oldOrder = this.getOne(lambdaQueryWrapper);
        if (oldOrder == null) {
            return null;
        }
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(oldOrder, orderVO);
        ProductInfo productInfo = productInfoService.getProductInfoById(oldOrder.getProductId());
        orderVO.setProductInfo(productInfo);
        orderVO.setTotalAmount(oldOrder.getPayAmount());
        return orderVO;
    }

    @Override
    public OrderVO saveProductOrder(Long productId, User loginUser) {
        ProductInfo productInfo = productInfoService.getProductInfoById(productId);
        if (productInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在");
        }

        Date date = DateUtil.date(System.currentTimeMillis());
        String orderSn = ORDER_PREFIX + RandomUtil.randomNumbers(20);
        // 订单信息初始化
        Order productOrder = new Order();
        productOrder.setUserId(loginUser.getId());
        productOrder.setOrderSn(orderSn);
        productOrder.setProductId(productInfo.getId());
        productOrder.setPayAmount(productInfo.getTotal());
        productOrder.setStatus(NOTPAY.getValue());
        productOrder.setPayType(ALIPAY.getValue());
        productOrder.setAddCoins(productInfo.getAddCoins());
        productOrder.setCreateTime(date);
        productOrder.setUpdateTime(date);

        boolean saveResult = this.save(productOrder);

        // 同时消息队列发送延时消息
        orderMqUtils.sendOrderSnInfo(productOrder);

        // 支付宝相关
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderSn);
        model.setSubject(productInfo.getName());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // 金额设置
        BigDecimal scaledAmount = new BigDecimal(productInfo.getTotal()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        model.setTotalAmount(String.valueOf(scaledAmount));
        model.setBody(productInfo.getDescription());
        AlipayClient alipayClient = new DefaultAlipayClient(
                "https://openapi.alipay.com/gateway.do",
                aliPayAccountConfig.getAppId(),
                aliPayAccountConfig.getPrivateKey(),
                "json", "UTF8",
                aliPayAccountConfig.getAliPayPublicKey(), "RSA2");


        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(aliPayAccountConfig.getNotifyUrl());
        request.setReturnUrl(aliPayAccountConfig.getReturnUrl());

        try {
            // 支付url
            AlipayTradePagePayResponse alipayTradePagePayResponse = alipayClient.pageExecute(request);
            String payUrl = alipayTradePagePayResponse.getBody();
            productOrder.setFormData(payUrl);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }

        boolean updateResult = this.updateProductOrder(productOrder);
        if (!updateResult & !saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        // 构建vo
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(productOrder, orderVO);
        orderVO.setProductInfo(productInfo);
        orderVO.setTotalAmount(productInfo.getTotal());
        return orderVO;
    }

    @Override
    public boolean updateProductOrder(Order productOrder) {
        String formData = productOrder.getFormData();
        Long id = productOrder.getId();
        Order updateCodeUrl = new Order();
        updateCodeUrl.setFormData(formData);
        updateCodeUrl.setId(id);
        return this.updateById(updateCodeUrl);
    }

    @Override
    public boolean updateOrderStatusByOrderNo(String outTradeSn, Integer orderStatus) {
        Order order = new Order();
        order.setStatus(orderStatus);
        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Order::getOrderSn, outTradeSn);
        return this.update(order, lambdaQueryWrapper);
    }

    @Override
    public void closedOrderByOrderNo(String outTradeNo) throws AlipayApiException {
        AlipayTradeCloseModel alipayTradeCloseModel = new AlipayTradeCloseModel();
        alipayTradeCloseModel.setOutTradeNo(outTradeNo);
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        request.setBizModel(alipayTradeCloseModel);
        AliPayApi.doExecute(request);
    }

    @Override
    public Order getOrderByOutTradeNo(String outTradeNo) {
        LambdaQueryWrapper<Order> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Order::getOrderSn, outTradeNo);
        return this.getOne(lambdaQueryWrapper);
    }


    @Override
    public String doPaymentNotify(String notifyData, HttpServletRequest request) {
//        Map<String, String> params = AliPayApi.toMap(request);
        String[] keyValuePairs = notifyData.split("&");
        Map<String, String> params = new HashMap<>();

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            key = StrUtil.toCamelCase(key);
            params.put(key, value);
        }
        AliPayAsyncResponse aliPayAsyncResponse = JSONUtil.toBean(JSONUtil.toJsonStr(params), AliPayAsyncResponse.class);
        String lockName = "notify:AlipayOrder:lock:" + aliPayAsyncResponse.getOutTradeNo();
        // 加锁
        return redissonLockUtil.redissonDistributedLocks(lockName, "【支付宝异步回调异常】:", () -> {
            String result;
            try {
                result = checkAlipayOrder(aliPayAsyncResponse, params);
            } catch (AlipayApiException e) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
            }
            if (!"success".equals(result)) {
                return result;
            }
            String doAliPayOrderBusinessResult = this.doAliPayOrderBusiness(aliPayAsyncResponse);
            if (StringUtils.isBlank(doAliPayOrderBusinessResult)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            return doAliPayOrderBusinessResult;
        });
    }

    /**
     * 检查订单
     *
     * @param response
     * @param params
     * @return
     * @throws AlipayApiException
     */
    private String checkAlipayOrder(AliPayAsyncResponse response, Map<String, String> params) throws AlipayApiException {
        String result = "failure";
//        String signType = params.get("signType");
//        String charset = params.get("charset");
//        boolean verifyResult = AlipaySignature.rsaCheckV1(params, aliPayAccountConfig.getAliPayPublicKey(),
//                charset,
//                signType);
//        if (!verifyResult) {
//            return result;
//        }
        // 1.验证该通知数据中的 out_trade_no 是否为商家系统中创建的订单号。
        Order order = this.getOrderByOutTradeNo(response.getOutTradeNo());
        if (order == null) {
            log.error("订单不存在");
            return result;
        }
        // 2.判断 total_amount 是否确实为该订单的实际金额（即商家订单创建时的金额）。
        int totalAmount = new BigDecimal(response.getTotalAmount()).multiply(new BigDecimal("100")).intValue();
        if (totalAmount != order.getPayAmount()) {
            log.error("订单金额不一致");
            return result;
        }
        // 3.校验通知中的 seller_id（或者 seller_email) 是否为 out_trade_no 这笔单据的对应的操作方（有的时候，一个商家可能有多个 seller_id/seller_email）。
        String sellerId = aliPayAccountConfig.getSellerId();
        if (!response.getSellerId().equals(sellerId)) {
            log.error("卖家账号校验失败");
            return result;
        }
        // 4.验证 app_id 是否为该商家本身。
        String appId = aliPayAccountConfig.getAppId();
        if (!response.getAppId().equals(appId)) {
            log.error("校验失败");
            return result;
        }
        // 状态 TRADE_SUCCESS 的通知触发条件是商家开通的产品支持退款功能的前提下，买家付款成功。
        String tradeStatus = response.getTradeStatus();
        if (!tradeStatus.equals("TRADE_SUCCESS")) {
            log.error("交易失败");
            return result;
        }
        return "success";
    }

    /**
     * 处理业务函数
     *
     * @param response
     * @return
     */
    @SneakyThrows
    protected String doAliPayOrderBusiness(AliPayAsyncResponse response) {
        String outTradeNo = response.getOutTradeNo();
        Order order = this.getOrderByOutTradeNo(outTradeNo);
        // 处理重复通知
        if (PAID.getValue().equals(order.getStatus())) {
            return "success";
        }
        // 业务代码
        // 更新订单状态
        boolean updateOrderStatus = this.updateOrderStatusByOrderNo(outTradeNo, PAID.getValue());

        // 保存支付记录
        PaymentInfoVO paymentInfoVO = new PaymentInfoVO();
        paymentInfoVO.setAppid(response.getAppId());
        paymentInfoVO.setOutTradeNo(response.getOutTradeNo());
        paymentInfoVO.setTransactionId(response.getTradeNo());
        paymentInfoVO.setTradeType("电脑网站支付");
        paymentInfoVO.setTradeState(response.getTradeStatus());
        paymentInfoVO.setTradeStateDesc("支付成功");
        paymentInfoVO.setSuccessTime(response.getNotifyTime());
        WxPayOrderQueryV3Result.Payer payer = new WxPayOrderQueryV3Result.Payer();
        payer.setOpenid(response.getBuyerId());
        paymentInfoVO.setPayer(payer);
        WxPayOrderQueryV3Result.Amount amount = new WxPayOrderQueryV3Result.Amount();
        amount.setTotal(new BigDecimal(response.getTotalAmount()).multiply(new BigDecimal("100")).intValue());
        amount.setPayerTotal(new BigDecimal(response.getReceiptAmount()).multiply(new BigDecimal("100")).intValue());
        amount.setCurrency("CNY");
        amount.setPayerCurrency("CNY");
        paymentInfoVO.setAmount(amount);
        boolean paymentResult = paymentInfoService.createPaymentInfo(paymentInfoVO);
        if (paymentResult && updateOrderStatus) {
            // 消息队列发送成功消息 发送异步消息增加用户的虚拟货币
            orderMqUtils.sendPaySuccess(order.getOrderSn());
            log.info("【支付回调通知处理成功】");
            return "success";
        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR);
    }
}
