package com.api.apiorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Data
@ConfigurationProperties(prefix = "alipay")
@Component
public class AliPayAccountConfig {
    /**
     * appId
     */
    private String appId;
    /**
     * 商户私钥
     */
    private String privateKey;
    /**
     * 支付宝公钥
     */
    private String aliPayPublicKey;
    /**
     * 异步通知url
     */
    private String notifyUrl;

    /**
     * 同步返回的url
     */
    private String returnUrl;

    /**
     * 是否使用沙箱
     */
    private Boolean sandbox;

    /**
     * 卖家id
     */
    private String sellerId;
}