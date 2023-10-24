package com.api.apiorder.service.impl;


import cn.hutool.json.JSONUtil;
import com.api.apiorder.mapper.PaymentInfoMapper;
import com.api.apiorder.model.entity.PaymentInfo;
import com.api.apiorder.model.vo.PaymentInfoVO;
import com.api.apiorder.service.PaymentInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.binarywang.wxpay.bean.result.WxPayOrderQueryV3Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo>
implements PaymentInfoService{

    @Override
    public boolean createPaymentInfo(PaymentInfoVO paymentInfoVO) {
        String transactionId = paymentInfoVO.getTransactionId();
        String tradeType = paymentInfoVO.getTradeType();
        String tradeState = paymentInfoVO.getTradeState();
        String tradeStateDesc = paymentInfoVO.getTradeStateDesc();
        String successTime = paymentInfoVO.getSuccessTime();
        WxPayOrderQueryV3Result.Payer payer = paymentInfoVO.getPayer();
        WxPayOrderQueryV3Result.Amount amount = paymentInfoVO.getAmount();

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderSn(paymentInfoVO.getOutTradeNo());
        paymentInfo.setTransactionId(transactionId);
        paymentInfo.setTradeType(tradeType);
        paymentInfo.setTradeState(tradeState);
        if (StringUtils.isNotBlank(successTime)) {
            paymentInfo.setSuccessTime(successTime);
        }
        paymentInfo.setOpenid(payer.getOpenid());
        paymentInfo.setPayerTotal(amount.getPayerTotal());
        paymentInfo.setCurrency(amount.getCurrency());
        paymentInfo.setPayerCurrency(amount.getPayerCurrency());
        paymentInfo.setTotal(amount.getTotal());
        paymentInfo.setTradeStateDesc(tradeStateDesc);
        paymentInfo.setContent(JSONUtil.toJsonStr(paymentInfoVO));
        return this.save(paymentInfo);
    }
}
