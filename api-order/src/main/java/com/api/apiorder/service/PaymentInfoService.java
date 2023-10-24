package com.api.apiorder.service;


import com.api.apiorder.model.entity.PaymentInfo;
import com.api.apiorder.model.vo.PaymentInfoVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    /**
     * 创建付款信息
     *
     * @param paymentInfoVO 付款信息vo
     * @return boolean
     */
    boolean createPaymentInfo(PaymentInfoVO paymentInfoVO);
}
