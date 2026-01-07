package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.converter.OrderWriteConvert;
import com.sky.dto.order.OrdersPaymentDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.exception.AddressBookException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.exception.UserNotFoundException;
import com.sky.mapper.*;
import com.sky.readmodel.ShoppingCartRM;
import com.sky.properties.WeChatProperties;
import com.sky.service.OrderService;
import com.sky.util.OrderAmountCalculator;
import com.sky.util.OrderNumberUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.sky.constant.MessageConstant.*;
import static com.sky.constant.OrderStatusConstant.PENDING_PAYMENT;
import static com.sky.constant.OrderStatusConstant.TO_BE_CONFIRMED;
import static com.sky.constant.PayStatusConstant.PAID;
import static com.sky.constant.PayStatusConstant.UN_PAID;
import lombok.RequiredArgsConstructor;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ShoppingCartMapper shoppingCartMapper;

    private final UserMapper userMapper;

    private final AddressBookMapper addressBookMapper;

    private final OrderWriteConvert orderWriteConvert;

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final WeChatPayUtil weChatPayUtil;
    private final WeChatProperties weChatProperties;

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

    @Transactional
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO dto) {
        if (dto == null || dto.getOrderNumber() == null || dto.getOrderNumber().isBlank()) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        Orders order = orderMapper.getByNumber(dto.getOrderNumber());
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        if (!PENDING_PAYMENT.equals(order.getStatus())) {
            throw new OrderBusinessException(ORDER_STATUS_ERROR);
        }
        if (dto.getPayMethod() != null && dto.getPayMethod() != 1) {
            throw new OrderBusinessException(PAY_METHOD_INVALID);
        }

        OrderPaymentVO paymentVO = buildMockPaymentVO(order.getNumber());

        /*
        // Real WeChat pay (requires valid sky.wechat.* config).
        User user = userMapper.getById(order.getUserId());
        if (user == null) {
            throw new UserNotFoundException(USER_NOT_FOUND);
        }
        JSONObject json = weChatPayUtil.pay(
                order.getNumber(),
                order.getAmount(),
                "Sky Takeout Order",
                user.getOpenid()
        );
        paymentVO = OrderPaymentVO.builder()
                .timeStamp(json.getString("timeStamp"))
                .nonceStr(json.getString("nonceStr"))
                .packageStr(json.getString("package"))
                .signType(json.getString("signType"))
                .paySign(json.getString("paySign"))
                .build();
        */

        markPaid(order.getNumber());

        return paymentVO;
    }

    private void markPaid(String orderNumber) {
        orderMapper.updateStatusByNumber(TO_BE_CONFIRMED, PAID, LocalDateTime.now(), orderNumber);
    }

    private OrderPaymentVO buildMockPaymentVO(String orderNumber) {
        String appid = defaultIfBlank(weChatProperties.getAppid(), "wx-mock-appid");
        String mchId = defaultIfBlank(weChatProperties.getMchid(), "1900000001");
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String packageStr = "prepay_id=mock_" + orderNumber;
        String signType = "RSA";
        String paySign = "MOCK_SIGN_" + appid + "_" + mchId;
        return OrderPaymentVO.builder()
                .nonceStr(nonceStr)
                .paySign(paySign)
                .timeStamp(timeStamp)
                .signType(signType)
                .packageStr(packageStr)
                .build();
    }

    private String defaultIfBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
