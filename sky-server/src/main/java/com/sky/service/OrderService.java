package com.sky.service;

import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {


    OrderSubmitVO submit(OrdersSubmitDTO dto);
}
