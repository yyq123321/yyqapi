package com.api.apiinterface.aop;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@RestControllerAdvice
public class InvokeCountAOP {

    // AOP切面
    public void doInvokeCnt() {}

}
