package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.converter.OrderWriteConvert;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.exception.AddressBookException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.exception.UserNotFoundException;
import com.sky.mapper.*;
import com.sky.readmodel.ShoppingCartRM;
import com.sky.service.OrderService;
import com.sky.util.OrderAmountCalculator;
import com.sky.util.OrderNumberUtil;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.MessageConstant.*;
import static com.sky.constant.OrderStatusConstant.PENDING_PAYMENT;
import static com.sky.constant.PayStatusConstant.UN_PAID;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private OrderWriteConvert orderWriteConvert;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO dto) {


        Long userId = BaseContext.getCurrentId();
        List<ShoppingCartRM> cartItems = shoppingCartMapper.listByUserId(userId); // 查询购物车快照
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ShoppingCartBusinessException(SHOPPING_CART_IS_NULL);
        }

        //计算金额
        BigDecimal calculatedAmount = OrderAmountCalculator.calcTotalAmount(cartItems);

        // 补全order
        User user = userMapper.getById(userId);
        if (user == null) {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        AddressBook address = addressBookMapper.getByIdAndUserId(dto.getAddressBookId(), userId);
        if (address == null) {
            throw new AddressBookException(ADDRESS_BOOK_NOT_FOUND);
        }

        // 忽略 amount
        Orders order = orderWriteConvert.fromCreateDTO(dto);
        order.setAmount(calculatedAmount);
        order.setNumber(OrderNumberUtil.nextOrderNumber());
        order.setStatus(PENDING_PAYMENT);
        order.setUserId(userId);
        order.setOrderTime(LocalDateTime.now());
        order.setPayStatus(UN_PAID);
        order.setUserName(user.getName());
        order.setConsignee(address.getConsignee());
        order.setPhone(address.getPhone()); // 更合理
        String fullAddress = address.getProvinceName()
                + address.getCityName()
                + address.getDistrictName()
                + address.getDetail();
        order.setAddress(fullAddress);
        // 回传了orderId
        orderMapper.insert(order);
        // 封装补全 order_detail
        List<OrderDetail> details = cartItems.stream()
                .map(cartItem -> (OrderDetail.builder()
                        .name(cartItem.getName())
                        .orderId(order.getId())
                        .dishId(cartItem.getDishId())
                        .setmealId(cartItem.getSetmealId())
                        .dishFlavor(cartItem.getDishFlavor())
                        .number(cartItem.getNumber())
                        .amount(cartItem.getAmount())
                        .image(cartItem.getImage())
                        .build()
                ))
                .toList();
        orderDetailMapper.insertBatch(details);
        // 清空购物车
        shoppingCartMapper.clean(userId);
        return OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();
    }
}
