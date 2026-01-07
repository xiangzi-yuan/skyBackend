package com.sky.service;

import com.sky.dto.order.OrdersPaymentDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {


    OrderSubmitVO submit(OrdersSubmitDTO dto);

    OrderPaymentVO payment(OrdersPaymentDTO dto);
}
