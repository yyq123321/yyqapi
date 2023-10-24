package com.api.apiorder.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

@Data
public class OrderCreateRequest {


    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long productId;


}
