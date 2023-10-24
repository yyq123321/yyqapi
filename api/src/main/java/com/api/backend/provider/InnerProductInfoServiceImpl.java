package com.api.backend.provider;

import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.model.entity.ProductInfo;
import com.api.apicommon.service.InnerProductInfoService;
import com.api.backend.mapper.ProductInfoMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@DubboService
public class InnerProductInfoServiceImpl implements InnerProductInfoService {

    @Resource
    private ProductInfoMapper productInfoMapper;

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

    @Override
    public ProductInfo getProductInfoById(Long productId) {
        return productInfoMapper.selectById(productId);
    }
}
