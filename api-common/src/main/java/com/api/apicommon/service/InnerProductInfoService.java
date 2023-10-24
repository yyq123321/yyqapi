package com.api.apicommon.service;


import com.api.apicommon.model.entity.ProductInfo;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface InnerProductInfoService{
    /**
     * 有效产品信息
     * 校验
     *
     * @param add         是否为创建校验
     * @param productInfo 产品信息
     */
    void validProductInfo(ProductInfo productInfo, boolean add);

    /**
     * 根据id获取
     * @param productId
     * @return
     */
    ProductInfo getProductInfoById(Long productId);
}
