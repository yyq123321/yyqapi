package com.api.backend.service;


import com.api.apicommon.model.entity.ProductInfo;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
public interface ProductInfoService extends IService<ProductInfo> {
    /**
     * 有效产品信息
     * 校验
     *
     * @param add         是否为创建校验
     * @param productInfo 产品信息
     */
    void validProductInfo(ProductInfo productInfo, boolean add);
}
