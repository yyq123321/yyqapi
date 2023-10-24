package com.api.apigateway.filter;

import com.api.apiclientsdk.utils.SignUtil;
import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.model.entity.InterfaceInfo;
import com.api.apicommon.model.entity.User;
import com.api.apicommon.service.InnerInterfaceInfoService;
import com.api.apicommon.service.InnerUserInterfaceInfoService;
import com.api.apicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    //白名单
    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    private static final String INTERFACE_HOST = "http://127.0.0.1:8123";
    private static final String DATABASE_PREFIX = "http://127.0.0.1";

    // 此处可修改
//    private static final String INTERFACE_HOST = "http://localhost:8123";

    /**
     * 五分钟过期时间
     */
    private static final long FIVE_MINUTES = 5L * 60;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.用户发送请求到API网关（已完成）

        // 2.请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String databasePath = DATABASE_PREFIX + request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一表示id: " + request.getId());
        log.info("请求路径: " + path);
        log.info("请求方法: " + method);
        log.info("请求参数: " + request.getQueryParams());
        String sourceAddress = request.getLocalAddress().getHostString();
        log.info("请求来源地址: " + request.getLocalAddress().getHostString());
        ServerHttpResponse response = exchange.getResponse();
        // 3.黑白名单
        if(!IP_WHITE_LIST.contains(sourceAddress)) {
            log.error("非白名单访问ip:" + request.getLocalAddress().getHostString());
            return handleNoAuth(response);
        }

        // 4.用户鉴权
        HttpHeaders headers = request.getHeaders();

        String accessKey = headers.getFirst("accessKey");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");

        // 请求头中参数必须完整
        if (StringUtils.isAnyBlank(body, sign, accessKey, timestamp)) {
            log.error("请求头参数不完整");
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR);
        }

        // 防重发XHR
        long currentTime = System.currentTimeMillis() / 1000;
        assert timestamp != null;
        if (currentTime - Long.parseLong(timestamp) >= FIVE_MINUTES) {
            log.error("会话已经过期");
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "会话已过期,请重试！");
        }

        // 数据库查询
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error!", e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }

        String secretKey = invokeUser.getSecretKey();

        // 校验签名
        String serverSign = SignUtil.getSign(body, secretKey);
        if(sign == null || !sign.equals(serverSign)) {
            log.error("校验签名错误");
            return handleNoAuth(response);
        }

        // 5.判断请求接口是否存在
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(databasePath, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error!", e);
        }
        if (interfaceInfo == null) {
            log.error("接口为空");
            return handleNoAuth(response);
        }
        if(interfaceInfo.getStatus() != 1) {
            log.error("接口未开放");
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "接口未开放");
        }
        Long price = interfaceInfo.getPrice();

        // 6.判断是否货币充足 货币充足会扣减货币
        boolean result = false;
        try {
            result = innerUserService.invokeCount(invokeUser.getId(), price);
        } catch (Exception e) {
            log.error("统计接口出现问题或者用户恶意调用不存在的接口");
            e.printStackTrace();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }

        if (!result){
            log.error("余额不足");
            return handleNoAuth(response);
        }

        // 7. 请求转发，调用模拟接口 + 响应日志
        return handleResponse(exchange, chain, interfaceInfo, invokeUser.getId());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }

    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }

    /**
     * 处理响应
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, InterfaceInfo interfaceInfo, long userId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓存数据的工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里写数据
                            // 拼接字符串
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // 7. 调用成功，接口调用次数 + 1
                                        try {
                                            innerUserInterfaceInfoService.invokeCount(interfaceInfo.getId(), userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }

                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);//释放掉内存
                                        // 构建日志
                                        StringBuilder sb2 = new StringBuilder(200);
                                        List<Object> rspArgs = new ArrayList<>();
                                        rspArgs.add(originalResponse.getStatusCode());
                                        String data = new String(content, StandardCharsets.UTF_8); //data
                                        sb2.append(data);
                                        // 打印日志
                                        log.info("响应结果：" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange); // 降级处理返回数据
        } catch (Exception e) {
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }
    }

}
