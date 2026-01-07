package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.converter.OrderWriteConvert;
import com.sky.dto.order.OrdersCancelDTO;
import com.sky.dto.order.OrdersConfirmDTO;
import com.sky.dto.order.OrdersPageQueryDTO;
import com.sky.dto.order.OrdersPaymentDTO;
import com.sky.dto.order.OrdersRejectionDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.AddressBookException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.exception.UserNotFoundException;
import com.sky.mapper.*;
import com.sky.readmodel.ShoppingCartRM;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.util.OrderAmountCalculator;
import com.sky.util.OrderNumberUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.order.OrderDetailVO;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderStatisticsVO;
import com.sky.vo.order.OrderSubmitVO;
import com.sky.vo.order.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sky.constant.MessageConstant.*;
import static com.sky.constant.OrderStatusConstant.*;
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
    private final WebSocketServer webSocketServer;

    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO dto) {

        Long userId = BaseContext.getCurrentId();
        List<ShoppingCartRM> cartItems = shoppingCartMapper.listByUserId(userId); // 查询购物车快照
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ShoppingCartBusinessException(SHOPPING_CART_IS_NULL);
        }

        // 计算金额
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
                        .build()))
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

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 【模拟支付】直接更新订单状态为已支付
        paySuccess(ordersPaymentDTO.getOrderNumber());

        // 【模拟支付】返回模拟的预支付数据
        return OrderPaymentVO.builder()
                .nonceStr("mock_nonce_" + System.currentTimeMillis())
                .paySign("mock_pay_sign")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .signType("RSA")
                .packageStr("prepay_id=mock_prepay_id")
                .build();
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(TO_BE_CONFIRMED)
                .payStatus(PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        // 通过WebSocket向商家推送新订单消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1); // 1=来单提醒
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号：" + outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClients(json);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getDetail(Long id) {
        // 查询订单
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 校验是否是当前用户的订单
        Long userId = BaseContext.getCurrentId();
        if (!order.getUserId().equals(userId)) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 查询订单明细
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);

        // 组装VO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        // 转换订单明细为 OrderDetailVO
        List<OrderDetailVO> detailVOList = details.stream()
                .map(detail -> {
                    OrderDetailVO vo = new OrderDetailVO();
                    BeanUtils.copyProperties(detail, vo);
                    return vo;
                })
                .toList();
        orderVO.setOrderDetailList(detailVOList);

        return orderVO;
    }

    /**
     * 历史订单分页查询
     *
     * @param dto
     * @return
     */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO dto) {
        // 设置当前用户id
        dto.setUserId(BaseContext.getCurrentId());

        // 分页查询
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(dto);

        // 为每个订单查询订单明细
        List<OrderVO> orderVOList = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);

                // 查询订单明细
                List<OrderDetail> details = orderDetailMapper.getByOrderId(order.getId());
                List<OrderDetailVO> detailVOList = details.stream()
                        .map(detail -> {
                            OrderDetailVO vo = new OrderDetailVO();
                            BeanUtils.copyProperties(detail, vo);
                            return vo;
                        })
                        .toList();
                orderVO.setOrderDetailList(detailVOList);

                orderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void cancel(Long id) {
        // 查询订单
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 校验是否是当前用户的订单
        Long userId = BaseContext.getCurrentId();
        if (!order.getUserId().equals(userId)) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 校验订单状态：只有待付款和待接单状态才能取消
        Integer status = order.getStatus();
        if (!PENDING_PAYMENT.equals(status) && !TO_BE_CONFIRMED.equals(status)) {
            throw new OrderBusinessException(ORDER_STATUS_ERROR);
        }

        // 如果是待接单状态（已支付），需要退款
        // 这里简化处理，实际需要调用微信退款接口
        if (TO_BE_CONFIRMED.equals(status)) {
            // TODO: 调用微信退款接口
            log.info("订单{}需要退款，已支付金额：{}", order.getNumber(), order.getAmount());
        }

        // 更新订单状态为已取消
        Orders updateOrder = Orders.builder()
                .id(id)
                .status(CANCELLED)
                .cancelReason("用户取消")
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(updateOrder);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        // 查询订单
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 校验是否是当前用户的订单
        Long userId = BaseContext.getCurrentId();
        if (!order.getUserId().equals(userId)) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 查询订单明细
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);

        // 将订单明细转换为购物车对象并插入
        for (OrderDetail detail : details) {
            ShoppingCart cart = ShoppingCart.builder()
                    .name(detail.getName())
                    .userId(userId)
                    .dishId(detail.getDishId())
                    .setmealId(detail.getSetmealId())
                    .dishFlavor(detail.getDishFlavor())
                    .number(detail.getNumber())
                    .amount(detail.getAmount())
                    .image(detail.getImage())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        // 查询订单
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 校验是否是当前用户的订单
        Long userId = BaseContext.getCurrentId();
        if (!order.getUserId().equals(userId)) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 通过WebSocket向商家推送催单消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 2); // 2=客户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + order.getNumber());
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClients(json);

        log.info("用户催单，订单id：{}，订单号：{}", id, order.getNumber());
    }

    // ===================== 管理端方法 =====================

    /**
     * 管理端订单搜索
     *
     * @param dto
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(dto);

        // 为每个订单查询订单明细并组装orderDishes
        List<OrderVO> orderVOList = new ArrayList<>();
        if (page != null && page.getTotal() > 0) {
            for (Orders order : page) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(order, orderVO);

                // 查询订单明细
                List<OrderDetail> details = orderDetailMapper.getByOrderId(order.getId());

                // 组装orderDishes字符串
                String orderDishes = details.stream()
                        .map(detail -> detail.getName() + "*" + detail.getNumber())
                        .collect(Collectors.joining("；"));
                orderVO.setOrderDishes(orderDishes);

                orderVOList.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 各状态订单统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 统计各状态订单数量
        Integer toBeConfirmed = orderMapper.countByStatus(TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(DELIVERY_IN_PROGRESS);

        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setToBeConfirmed(toBeConfirmed);
        vo.setConfirmed(confirmed);
        vo.setDeliveryInProgress(deliveryInProgress);
        return vo;
    }

    /**
     * 管理端查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getDetailAdmin(Long id) {
        // 查询订单
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 查询订单明细
        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);

        // 组装VO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);

        // 转换订单明细为 OrderDetailVO
        List<OrderDetailVO> detailVOList = details.stream()
                .map(detail -> {
                    OrderDetailVO vo = new OrderDetailVO();
                    BeanUtils.copyProperties(detail, vo);
                    return vo;
                })
                .toList();
        orderVO.setOrderDetailList(detailVOList);

        return orderVO;
    }

    /**
     * 接单
     *
     * @param dto
     */
    @Override
    public void confirm(OrdersConfirmDTO dto) {
        Orders order = Orders.builder()
                .id(dto.getId())
                .status(CONFIRMED)
                .build();
        orderMapper.update(order);
    }

    /**
     * 拒单
     *
     * @param dto
     */
    @Override
    public void rejection(OrdersRejectionDTO dto) {
        // 查询订单
        Orders orderDB = orderMapper.getById(dto.getId());
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 只有待接单状态的订单才能拒单
        if (!TO_BE_CONFIRMED.equals(orderDB.getStatus())) {
            throw new OrderBusinessException(ORDER_STATUS_ERROR);
        }

        // 如果已支付，需要退款
        if (PAID.equals(orderDB.getPayStatus())) {
            // TODO: 调用微信退款接口
            log.info("订单{}拒单，需要退款，已支付金额：{}", orderDB.getNumber(), orderDB.getAmount());
        }

        Orders order = Orders.builder()
                .id(dto.getId())
                .status(CANCELLED)
                .rejectionReason(dto.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
    }

    /**
     * 管理端取消订单
     *
     * @param dto
     */
    @Override
    public void cancelAdmin(OrdersCancelDTO dto) {
        // 查询订单
        Orders orderDB = orderMapper.getById(dto.getId());
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 如果已支付，需要退款
        if (PAID.equals(orderDB.getPayStatus())) {
            // TODO: 调用微信退款接口
            log.info("管理员取消订单{}，需要退款，已支付金额：{}", orderDB.getNumber(), orderDB.getAmount());
        }

        Orders order = Orders.builder()
                .id(dto.getId())
                .status(CANCELLED)
                .cancelReason(dto.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        // 查询订单
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 只有已接单状态的订单才能派送
        if (!CONFIRMED.equals(orderDB.getStatus())) {
            throw new OrderBusinessException(ORDER_STATUS_ERROR);
        }

        Orders order = Orders.builder()
                .id(id)
                .status(DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(order);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        // 查询订单
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 只有派送中状态的订单才能完成
        if (!DELIVERY_IN_PROGRESS.equals(orderDB.getStatus())) {
            throw new OrderBusinessException(ORDER_STATUS_ERROR);
        }

        Orders order = Orders.builder()
                .id(id)
                .status(COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
    }
}
