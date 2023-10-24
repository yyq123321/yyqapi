package com.api.apicommon.model.entity;


import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 订单
 * @TableName api_order
 */
@TableName(value = "api_order")
@Data
public class Order implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 订单应付金额
     */
    private Long payAmount;

    /**
     * 逻辑删除 0删除 1正常
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 二维码地址
     */
    private String codeUrl;

    /**
     * 增加虚拟货币数
     */
    private Long addCoins;

    /**
     * 支付方式 1-支付宝 2-微信
     */
    private Integer payType;

    /**
     * 商品id
     */
    private Long productId;

    /**
     * 订单状态 0-未支付 1 -已支付 2-超时支付
     */
    private Integer status;

    /**
     * 支付宝订单体
     */
    private String formData;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}