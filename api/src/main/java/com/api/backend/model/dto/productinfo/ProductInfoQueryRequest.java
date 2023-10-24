package com.api.backend.model.dto.productinfo;


import com.api.apicommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProductInfoQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 产品名称
     */
    private String name;
    /**
     * 增加虚拟货币个数
     */
    private Long addCoins;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 金额(分)
     */
    private Long total;

    /**
     * 产品类型（VIP-会员 RECHARGE-充值）
     */
    private String productType;

}