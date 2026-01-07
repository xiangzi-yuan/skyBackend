package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.dish.DishOverViewVO;
import com.sky.vo.order.OrderOverViewVO;
import com.sky.vo.setmeal.SetmealOverViewVO;

import java.time.LocalDateTime;

/**
 * 工作台服务接口
 */
public interface WorkspaceService {

    /**
     * 获取运营数据
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 获取订单概览数据
     *
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 获取菜品总览
     *
     * @return
     */
    DishOverViewVO getDishOverView();

    /**
     * 获取套餐总览
     *
     * @return
     */
    SetmealOverViewVO getSetmealOverView();
}
