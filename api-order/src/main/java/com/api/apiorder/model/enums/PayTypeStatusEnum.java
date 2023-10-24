package com.api.apiorder.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 支付类型枚举
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public enum PayTypeStatusEnum {

    /**
     * 微信支付
     */
    WX("微信支付", 2),
    /**
     * 支付宝支付
     */
    ALIPAY("支付宝支付", 1);

    private final String text;

    private final Integer value;

    PayTypeStatusEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
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
