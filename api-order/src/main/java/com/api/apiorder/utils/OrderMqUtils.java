package com.api.apiorder.utils;

import cn.hutool.core.util.IdUtil;

import com.api.apicommon.model.entity.Order;
import com.api.apiorder.config.RabbitmqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */
@Slf4j
@Component
public class OrderMqUtils implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {


    @Resource
    private RabbitTemplate rabbitTemplate;

    private Long finalId = null;

    /**
     * 向mq发送订单消息
     *
     * @param order
     */
    public void sendOrderSnInfo(Order order) {
        finalId = order.getId();
        String finalMessageId = IdUtil.simpleUUID();

        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_ORDER_PAY, RabbitmqConfig.ROUTINGKEY_ORDER_PAY, order, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //生成全局唯一id
            messageProperties.setMessageId(finalMessageId);
            //设置消息的有效时间
            message.getMessageProperties().setExpiration("600000"); //10分钟
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }

    /**
     * 1、只要消息抵达服务器，那么b=true
     *
     * @param correlationData 当前消息的唯一关联数据（消息的唯一id）
     * @param success         消息是否成功收到
     * @param cause           失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean success, String cause) {
        if (!success) {
            log.error("订单--消息投递到服务端失败：{}---->{}", correlationData, cause);
        }
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.error("发生异常，返回消息回调:{}", returned);
    }

    /**
     * 订单成功支付，发送异步消息处理更新虚拟货币
     * @param orderSn
     */
    public void sendPaySuccess(String orderSn) {
        String finalMessageId = IdUtil.simpleUUID();
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_ORDER_PAY_SUCCESS, RabbitmqConfig.ROUTINGKEY_ORDER_PAY_SUCCESS, orderSn, message -> {
            MessageProperties messageProperties = message.getMessageProperties();
            //生成全局唯一id
            messageProperties.setMessageId(finalMessageId);
            //设置消息的有效时间
            message.getMessageProperties().setExpiration("600000"); //10分钟
            messageProperties.setContentEncoding("utf-8");
            return message;
        });
    }
}
