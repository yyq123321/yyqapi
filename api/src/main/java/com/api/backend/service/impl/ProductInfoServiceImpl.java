package com.api.backend.service.impl;


import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.model.entity.ProductInfo;
import com.api.backend.mapper.ProductInfoMapper;
import com.api.backend.service.ProductInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Service
public class ProductInfoServiceImpl extends ServiceImpl<ProductInfoMapper, ProductInfo>
        implements ProductInfoService {


    @Override
    public void validProductInfo(ProductInfo productInfo, boolean add) {
        if (productInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = productInfo.getName();
        String description = productInfo.getDescription();
        Long total = productInfo.getTotal();
        Date expirationTime = productInfo.getExpirationTime();
        String productType = productInfo.getProductType();
        Long addCoins = productInfo.getAddCoins();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (addCoins < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "增加虚拟货币不能为负数");
        }
        if (total < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "售卖金额不能为负数");
        }
    }
}
