package com.api.apiorder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 声明订单支付超时需要用到的交换机和队列
 */
@Configuration
@Slf4j
public class RabbitmqConfig {

    public static final String QUEUE_ORDER_PAY = "queue_order_pay";
    public static final String EXCHANGE_ORDER_PAY ="exchange_order_pay";
    public static final String ROUTINGKEY_ORDER_PAY ="routing.order.pay";
    public static final String ROUTINGKEY_ORDER_PAY_SUCCESS ="routing.order.pay.success";

    public static final String QUEUE_ORDER_PAY_SUCCESS = "queue_order_pay_success";

    public static final String EXCHANGE_ORDER_PAY_SUCCESS ="exchange_order_pay_success";

    //定义死信队列
    public static final String QUEUE_ORDER_DLX_QUEUE = "queue_order_dlx_queue";


    //定义死信交换机
    public static final String EXCHANGE_ORDER_DLX_EXCHANGE = "exchange_order_dlx_exchange";

    //绑定
    public static final String ROUTINGKEY_DLX_ORDER_PAY ="routing.dlx.order.pay";


    //声明交换机
    @Bean(EXCHANGE_ORDER_PAY)
    public Exchange EXCHANGE_ORDER_PAY(){
        return new DirectExchange(EXCHANGE_ORDER_PAY,true,false);
    }

    // 声明支付成功交换机
    @Bean(EXCHANGE_ORDER_PAY_SUCCESS)
    public Exchange EXCHANGE_ORDER_PAY_SUCCESS(){
        return new DirectExchange(EXCHANGE_ORDER_PAY_SUCCESS,true,false);
    }


    //声明死信交换机
    @Bean(EXCHANGE_ORDER_DLX_EXCHANGE)
    public Exchange EXCHANGE_ORDER_DLX_EXCHANGE(){
        return new DirectExchange(EXCHANGE_ORDER_DLX_EXCHANGE,true,false);
    }


    //声明队列
    @Bean(QUEUE_ORDER_PAY)
    public Queue QUEUE_ORDER_PAY(){
        Map<String, Object> args = new HashMap<>(2);
        //正常队列中的消息被废弃后会被路由到死信队列(前提是有绑定死信队列)
        // 绑定我们的死信交换机
        args.put("x-dead-letter-exchange", EXCHANGE_ORDER_DLX_EXCHANGE);
        // 绑定我们的路由key
        args.put("x-dead-letter-routing-key", ROUTINGKEY_DLX_ORDER_PAY);
        args.put("x-message-ttl", 600000);
        // 这里测试1分钟
//        args.put("x-message-ttl", 60000);

        return new Queue(QUEUE_ORDER_PAY,true,false,false,args);
    }

    //声明支付成功队列
    @Bean(QUEUE_ORDER_PAY_SUCCESS)
    public Queue QUEUE_ORDER_PAY_SUCCESS(){
        Map<String, Object> args = new HashMap<>(2);
        //正常队列中的消息被废弃后会被路由到死信队列(前提是有绑定死信队列)
        // 绑定我们的死信交换机
        args.put("x-dead-letter-exchange", EXCHANGE_ORDER_DLX_EXCHANGE);
        // 绑定我们的路由key
        args.put("x-dead-letter-routing-key", ROUTINGKEY_DLX_ORDER_PAY);
        args.put("x-message-ttl", 600000);
        // 这里测试1分钟
//        args.put("x-message-ttl", 60000);

        return new Queue(QUEUE_ORDER_PAY_SUCCESS,true,false,false,args);
    }


    //声明死信队列
    @Bean(QUEUE_ORDER_DLX_QUEUE)
    public Queue QUEUE_ORDER_DLX_QUEUE(){
        return new Queue(QUEUE_ORDER_DLX_QUEUE,true,false,false);
    }

    //交换机绑定队列
    @Bean
    public Binding BINDING_QUEUE_ORDER_PAY(){
        return new Binding(QUEUE_ORDER_PAY,
                Binding.DestinationType.QUEUE, EXCHANGE_ORDER_PAY,
                ROUTINGKEY_ORDER_PAY,null);
    }

    //交换机绑定支付成功队列
    @Bean
    public Binding BINDING_QUEUE_ORDER_PAY_SUCCESS(){
        return new Binding(QUEUE_ORDER_PAY_SUCCESS,
                Binding.DestinationType.QUEUE, EXCHANGE_ORDER_PAY_SUCCESS,
                ROUTINGKEY_ORDER_PAY_SUCCESS,null);
    }


    //死信交换机绑定死信队列
    @Bean
    public Binding BINDING_DLX_QUEUE_ORDER_PAY(){
        return new Binding(QUEUE_ORDER_DLX_QUEUE,
                Binding.DestinationType.QUEUE, EXCHANGE_ORDER_DLX_EXCHANGE,
                ROUTINGKEY_DLX_ORDER_PAY,null);
    }

    // 配置 RabbitMQ 模板
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        // 根据你的实际需求配置 RabbitMQ 模板
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        // 其他配置...
        return rabbitTemplate;
    }

    // 配置 RabbitMQ 管理器
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        // 根据你的实际需求配置 RabbitMQ 管理器
        return new RabbitAdmin(connectionFactory);
    }


}