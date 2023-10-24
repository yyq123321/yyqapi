package com.api.apiorder.model.enums;

import lombok.Getter;

import static com.api.apiorder.model.enums.PaymentStatusEnum.*;


/**
 * @description: 交易状态枚举
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Getter
public enum AlipayTradeStatusEnum {

    /**
     * 交易创建，等待买家付款。
     */
    WAIT_BUYER_PAY(NOTPAY),

    /**
     * <pre>
     * 在指定时间段内未支付时关闭的交易；
     * 在交易完成全额退款成功时关闭的交易。
     * </pre>
     */
    TRADE_CLOSED(PAYOVERTIME),

    /**
     * 交易成功，且可对该交易做操作，如：多级分润、退款等。
     */
    TRADE_SUCCESS(PAID),

    /**
     * 等待卖家收款（买家付款后，如果卖家账号被冻结）。
     */
    TRADE_PENDING(NOTPAY),

    /**
     * 交易成功且结束，即不可再做任何操作。
     */
    TRADE_FINISHED(PAID);

    private final PaymentStatusEnum paymentStatusEnum;

    AlipayTradeStatusEnum(PaymentStatusEnum orderStatusEnum) {
        this.paymentStatusEnum = orderStatusEnum;
    }

    /**
     * 按名称查找
     *
     * @param name 名称
     * @return {@link AlipayTradeStatusEnum}
     */
    public static AlipayTradeStatusEnum findByName(String name) {
        for (AlipayTradeStatusEnum statusEnum : AlipayTradeStatusEnum.values()) {
            if (name.equalsIgnoreCase(statusEnum.name())) {
                return statusEnum;
            }
        }
        throw new RuntimeException("错误的支付宝支付状态");
    }
}