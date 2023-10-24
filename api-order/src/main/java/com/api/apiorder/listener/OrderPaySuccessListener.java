package com.api.apiorder.listener;


import com.api.apicommon.model.entity.Order;
import com.api.apicommon.service.InnerUserService;
import com.api.apiorder.config.RabbitmqConfig;
import com.api.apiorder.service.OrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.api.apicommon.common.ErrorCode;
import com.api.apicommon.exception.BusinessException;
import com.api.apicommon.service.ApiBackendService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.api.apicommon.constant.RabbitmqConstant.ORDER_SUCCESS_QUEUE_NAME;
import static com.api.apicommon.constant.RedisConstant.EXIST_KEY_VALUE;
import static com.api.apicommon.constant.RedisConstant.SEND_ORDER_PAY_SUCCESS_INFO;

/**
 * @description:
 * @author: Yiqi Yu
 * @time: 2023/10/22 23:15
 */

@Component
@Slf4j
public class OrderPaySuccessListener {

    @Resource
    private RedisTemplate<String, String> redisTemplate;


    @Resource
    private OrderService orderService;

    @DubboReference
    private InnerUserService innerUserService;

    public static final Integer ORDER_PAY_SUCCESS_STATUS = 1;

    //消息队列发送端的可靠性机制，保障发送端的消息成功路由到队列中
    public static final String CONSUME_ORDER_PAY_SUCCESS_INFO = "rabbitmq:consume:order:paySuccess:message:";


    //监听order.pay.success.queue订单交易成功队列，实现订单状态的修改以及给用户分配购买的接口调用次数
    //生产者是懒加载机制，消费者是饿汉加载机制，二者机制不对应，所以消费者要自行创建队列并加载，否则会报错
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queuesToDeclare = {@Queue(RabbitmqConfig.QUEUE_ORDER_PAY_SUCCESS)})
    public void receiveOrderMsg(String outTradeNo, Message message, Channel channel) throws IOException {

        //1.消息的可靠机制保障，如果消息成功被监听到说明消息已经成功由生产者将消息发送到队列中，不需要消息队列重新发送消息，删掉redis中对于消息的记录(发送端的消息可靠机制)
        redisTemplate.delete(SEND_ORDER_PAY_SUCCESS_INFO + outTradeNo);

        //2.消费端的消息幂等性问题，因为消费端开启手动确认机制，会有消息重复消费的问题，这里使用redis记录已经成功处理的订单来解决(消费端的消息可靠机制)
        String orderSn = redisTemplate.opsForValue().get(CONSUME_ORDER_PAY_SUCCESS_INFO + outTradeNo);

        if (StringUtils.isNoneBlank(orderSn)) {
            // 投递状态
            // false表示只确认当前消息，true表示会确认deliveryTag及其之前的消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 3.修改订单状态
        UpdateWrapper<Order> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", ORDER_PAY_SUCCESS_STATUS);
        updateWrapper.eq("orderSn", outTradeNo);
        boolean update = orderService.update(updateWrapper);


        // 4.给用户分配购买的虚拟货币次数
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("orderSn", outTradeNo);
        Order order = orderService.getOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "订单号不存在");
        }

        Long userId = order.getUserId();
        Long addCoins = order.getAddCoins();

        // 5.更新用户的虚拟货币余额
        boolean updateAddCoins = innerUserService.updateUserBalance(userId, addCoins);

        if (!update || !updateAddCoins) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            return;
        }

        // 6.为解决消费端的消息幂等性问题，记录已经的成功处理的消息。10分钟后订单已经结束，淘汰记录的订单消息
        redisTemplate.opsForValue().set(CONSUME_ORDER_PAY_SUCCESS_INFO + outTradeNo, EXIST_KEY_VALUE, 10, TimeUnit.MINUTES);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }


}