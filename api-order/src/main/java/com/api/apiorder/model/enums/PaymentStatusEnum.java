package com.api.apiorder.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 支付状态枚举
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public enum PaymentStatusEnum {

//    /**
//     * 支付成功
//     */
//    SUCCESS("支付成功", "SUCCESS"),
//
//    /**
//     * 支付失败
//     */
//    PAY_ERROR("支付失败", "PAYERROR"),
//    /**
//     * 用户付费中
//     */
//    USER_PAYING("用户支付中", "USER_PAYING"),
//    /**
//     * 已关闭
//     */
//    CLOSED("已关闭", "CLOSED"),

    /**
     * 未支付
     */
    NOTPAY("未支付", 0),

    /**
     * 已支付
     */
    PAID("已支付", 1),

    /**
     * 超时支付
     */
    PAYOVERTIME("超时支付", 2);
//    /**
//     * 转入退款
//     */
//    REFUND("转入退款", "REFUND"),
//    /**
//     * 退款中
//     */
//    PROCESSING("退款中", "PROCESSING"),
//    /**
//     * 撤销
//     */
//    REVOKED("已撤销（刷卡支付）", "REVOKED"),
//
//    /**
//     * 未知
//     */
//    UNKNOW("未知状态", "UNKNOW");


    private final String text;

    private final Integer value;

    PaymentStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值
     * 得到值
     * 获取值列表
     *
     * @return {@link List}<{@link Integer}>
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public Integer getValue() {
        return value;
    }

    public String getText() {
        return text;
    }
}
