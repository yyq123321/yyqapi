package com.api.apicommon.model.vo;

import cn.hutool.log.Log;
import com.api.apicommon.model.entity.ProductInfo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 */
@Data
public class OrderVO implements Serializable {


    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 订单号
     */
    private String orderNumber;


    /**
     * 交易金额
     */
    private Long totalAmount;

    /**
     * 虚拟货币增加数
     */
    private Long addCoins;

    /**
     * 交易状态【0->待付款；1->已完成；2->无效订单】
     */
    private Integer status;

    /**
     * 支付二维码地址
     */
    private String codeUrl;

    /**
     * 支付宝订单体
     */
    private String formData;

    /**
     * 创建时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;

    /**
     * 过期时间
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date expirationTime;

    /**
     * 商品信息
     */
    private ProductInfo productInfo;

    private static final long serialVersionUID = 1L;

}
