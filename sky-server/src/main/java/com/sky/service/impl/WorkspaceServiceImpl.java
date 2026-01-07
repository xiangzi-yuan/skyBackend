package com.sky.service.impl;

import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.dish.DishOverViewVO;
import com.sky.vo.order.OrderOverViewVO;
import com.sky.vo.setmeal.SetmealOverViewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static com.sky.constant.OrderStatusConstant.*;
import static com.sky.constant.StatusConstant.*;

/**
 * 工作台服务实现类
 */
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    /**
     * 获取运营数据
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // 营业额：状态为"已完成"的订单金额合计
        Double turnover = orderMapper.sumAmountByStatusAndTime(COMPLETED, begin, end);
        turnover = turnover == null ? 0.0 : turnover;

        // 有效订单数：状态为"已完成"的订单数量
        Integer validOrderCount = orderMapper.countByStatusAndTime(COMPLETED, begin, end);
        validOrderCount = validOrderCount == null ? 0 : validOrderCount;

        // 订单总数
        Integer totalOrderCount = orderMapper.countByStatusAndTime(null, begin, end);
        totalOrderCount = totalOrderCount == null ? 0 : totalOrderCount;

        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        // 平均客单价
        Double unitPrice = 0.0;
        if (validOrderCount != 0) {
            unitPrice = turnover / validOrderCount;
        }

        // 新增用户数
        Integer newUsers = userMapper.countByCreateTime(begin, end);
        newUsers = newUsers == null ? 0 : newUsers;

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 获取订单概览数据
     *
     * @return
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        // 待接单
        Integer waitingOrders = orderMapper.countByStatusAndTime(TO_BE_CONFIRMED, begin, end);
        // 待派送（已接单）
        Integer deliveredOrders = orderMapper.countByStatusAndTime(CONFIRMED, begin, end);
        // 已完成
        Integer completedOrders = orderMapper.countByStatusAndTime(COMPLETED, begin, end);
        // 已取消
        Integer cancelledOrders = orderMapper.countByStatusAndTime(CANCELLED, begin, end);
        // 全部订单
        Integer allOrders = orderMapper.countByStatusAndTime(null, begin, end);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 获取菜品总览
     *
     * @return
     */
    @Override
    public DishOverViewVO getDishOverView() {
        Integer sold = dishMapper.countByStatus(ENABLE);
        Integer discontinued = dishMapper.countByStatus(DISABLE);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 获取套餐总览
     *
     * @return
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = setmealMapper.countByStatus(ENABLE);
        Integer discontinued = setmealMapper.countByStatus(DISABLE);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
