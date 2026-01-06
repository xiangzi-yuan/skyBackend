package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.converter.OrderWriteConvert;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.utils.OrderNumberUtil;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.sky.constant.OrderStatusConstant.PENDING_PAYMENT;
import static com.sky.constant.PayStatusConstant.UN_PAID;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {


    private final OrderWriteConvert orderWriteConvert;
    private final UserMapper userMapper;
    private final AddressBookMapper addressBookMapper;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderWriteConvert orderWriteConvert, UserMapper userMapper, AddressBookMapper addressBookMapper, OrderMapper orderMapper) {
        this.orderWriteConvert = orderWriteConvert;
        this.userMapper = userMapper;
        this.addressBookMapper = addressBookMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO dto) {
        // 校验环节
        Long userId = BaseContext.getCurrentId();
        // 补全order
        User user = userMapper.getById(userId);
        AddressBook address = addressBookMapper.getById(dto.getAddressBookId());


        Orders order = orderWriteConvert.fromCreateDTO(dto);

        order.setNumber(OrderNumberUtil.nextOrderNumber());
        order.setStatus(PENDING_PAYMENT);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(UN_PAID);
        order.setUserName(user.getName());
        order.setPhone(user.getPhone());
        // address：建议拼完整地址，不只 detail
        order.setAddress(address.getDetail());

        // orderMapper.insert(order);
        // 补全 order_detail
        // mapper
        // 返回
        return null;
    }
}
