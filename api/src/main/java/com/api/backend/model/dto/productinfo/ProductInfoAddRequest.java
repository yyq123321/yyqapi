package com.api.backend.model.dto.productinfo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Data
public class ProductInfoAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 金额(分)
     */
    private Long total;

    /**
     * 增加虚拟货币个数
     */
    private Long addCoins;

    /**
     * 产品类型（VIP-会员 RECHARGE-充值）
     */
    private String productType;

    /**
     * 过期时间
     */
    private Date expirationTime;
}