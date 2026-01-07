package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.OrderStatusConstant.*;

/**
 * 订单定时任务
 * 1. 自动取消超时未支付订单
 * 2. 自动完成前一天派送中的订单
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderTask {

    private final OrderMapper orderMapper;

    /**
     * 处理超时未支付订单
     * 每分钟执行一次，取消超过15分钟未支付的订单
     */
    @Scheduled(cron = "0 * * * * ?") // 每分钟触发一次
    public void processTimeoutOrder() {
        log.info("定时任务：处理超时未支付订单，执行时间：{}", LocalDateTime.now());

        // 查询15分钟前下单且状态为待付款的订单
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(PENDING_PAYMENT, time);

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                Orders updateOrder = Orders.builder()
                        .id(order.getId())
                        .status(CANCELLED)
                        .cancelReason("订单超时，自动取消")
                        .cancelTime(LocalDateTime.now())
                        .build();
                orderMapper.update(updateOrder);
                log.info("已取消超时订单，订单号：{}", order.getNumber());
            }
        }
    }

    /**
     * 处理前一天派送中但未完成的订单
     * 每天凌晨1点执行，自动完成前一天的派送中订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点触发
    public void processDeliveryOrder() {
        log.info("定时任务：处理派送中订单，执行时间：{}", LocalDateTime.now());

        // 查询前一天（当前时间减1小时，即凌晨0点之前）仍处于派送中的订单
        LocalDateTime time = LocalDateTime.now().minusMinutes(60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(DELIVERY_IN_PROGRESS, time);

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders order : ordersList) {
                Orders updateOrder = Orders.builder()
                        .id(order.getId())
                        .status(COMPLETED)
                        .deliveryTime(LocalDateTime.now())
                        .build();
                orderMapper.update(updateOrder);
                log.info("已自动完成订单，订单号：{}", order.getNumber());
            }
        }
    }
}
