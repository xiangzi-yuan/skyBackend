package com.sky.service;

import com.sky.dto.order.OrdersCancelDTO;
import com.sky.dto.order.OrdersConfirmDTO;
import com.sky.dto.order.OrdersPageQueryDTO;
import com.sky.dto.order.OrdersPaymentDTO;
import com.sky.dto.order.OrdersRejectionDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderStatisticsVO;
import com.sky.vo.order.OrderSubmitVO;
import com.sky.vo.order.OrderVO;

public interface OrderService {

    OrderSubmitVO submit(OrdersSubmitDTO dto);

    /**
     * 订单支付
     * 
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * 
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 查询订单详情
     * 
     * @param id
     * @return
     */
    OrderVO getDetail(Long id);

    /**
     * 历史订单分页查询
     * 
     * @param dto
     * @return
     */
    PageResult historyOrders(OrdersPageQueryDTO dto);

    /**
     * 用户取消订单
     * 
     * @param id
     */
    void cancel(Long id);

    /**
     * 再来一单
     * 
     * @param id
     */
    void repetition(Long id);

    /**
     * 催单
     * 
     * @param id
     */
    void reminder(Long id);

    // ===================== 管理端方法 =====================

    /**
     * 管理端订单搜索
     * 
     * @param dto
     * @return
     */
    PageResult conditionSearch(OrdersPageQueryDTO dto);

    /**
     * 各状态订单统计
     * 
     * @return
     */
    OrderStatisticsVO statistics();

    /**
     * 管理端查询订单详情
     * 
     * @param id
     * @return
     */
    OrderVO getDetailAdmin(Long id);

    /**
     * 接单
     * 
     * @param dto
     */
    void confirm(OrdersConfirmDTO dto);

    /**
     * 拒单
     * 
     * @param dto
     */
    void rejection(OrdersRejectionDTO dto);

    /**
     * 管理端取消订单
     * 
     * @param dto
     */
    void cancelAdmin(OrdersCancelDTO dto);

    /**
     * 派送订单
     * 
     * @param id
     */
    void delivery(Long id);

    /**
     * 完成订单
     * 
     * @param id
     */
    void complete(Long id);
}
