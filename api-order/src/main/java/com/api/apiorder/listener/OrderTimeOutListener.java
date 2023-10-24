package com.api.apiorder.listener;


import com.api.apicommon.model.entity.Order;
import com.api.apicommon.service.InnerUserService;
import com.api.apiorder.config.RabbitmqConfig;
import com.api.apiorder.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.api.apicommon.service.ApiBackendService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

@Component
@Slf4j
public class OrderTimeOutListener {

    @DubboReference
    private ApiBackendService apiBackendService;

    @DubboReference
    private InnerUserService innerUserService;

    @Resource
    private OrderService orderService;

    public static final Integer ORDER_TIMEOUT_STATUS = 2;
    public static final Integer ORDER_UNPAY_STATUS = 0;

    //监听queue_order_dlx_queue死信队列，实现支付超时的回滚功能
    //生产者是懒加载机制，消费者是饿汉加载机制，二者机制不对应，所以消费者要自行创建队列并加载，否则会报错
    @RabbitListener(queuesToDeclare = {@Queue(RabbitmqConfig.QUEUE_ORDER_DLX_QUEUE)})
    public void receiveOrderMsg(Order order, Message message, Channel channel) throws IOException {

        log.info("监听到消息啦，内容是：" + order.toString());
        Order dbOrder = orderService.getById(order.getId());
        Long addCoins = dbOrder.getAddCoins();
        Long userId = dbOrder.getUserId();

        //根据订单状态判断订单是否支付成功，如果没有支付成功则回滚
        if (dbOrder.getStatus().equals(ORDER_UNPAY_STATUS)) {
            try {
                // 回滚用户的虚拟货币余额
                boolean success = innerUserService.recoverUserBalance(userId, addCoins);
                if (!success) {
                    log.error("回滚库存失败!!!");
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
                }
                UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
                updateWrapper.set("status", ORDER_TIMEOUT_STATUS);
                updateWrapper.eq("id", dbOrder.getId());
                orderService.update(updateWrapper);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (Exception e) {
                log.error("回滚库存失败!!!");
                e.printStackTrace();
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        }


        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


}