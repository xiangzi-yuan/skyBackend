package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.converter.order.OrderReadConvert;
import com.sky.converter.order.OrderWriteConvert;
import com.sky.dto.order.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.exception.UserNotFoundException;
import com.sky.mapper.*;
import com.sky.readmodel.ShoppingCartRM;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.util.OrderAmountCalculator;
import com.sky.util.OrderNumberUtil;
import com.sky.vo.order.*;
import com.sky.websocket.WebSocketServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.sky.constant.MessageConstant.*;
import static com.sky.constant.OrderStatusConstant.*;
import static com.sky.constant.PayStatusConstant.PAID;
import static com.sky.constant.PayStatusConstant.UN_PAID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ShoppingCartMapper shoppingCartMapper;

    private final UserMapper userMapper;

    private final AddressBookMapper addressBookMapper;

    private final OrderWriteConvert orderWriteConvert;
    private final OrderReadConvert orderReadConvert;

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
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
                .collect(Collectors.toList());
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
        if (ordersDB == null) {
            log.warn("支付回调订单不存在，订单号: {}", outTradeNo);
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        if (PAID.equals(ordersDB.getPayStatus())) {
            log.info("订单已支付，幂等处理跳过，订单号: {}", outTradeNo);
            return;
        }

        Integer status = ordersDB.getStatus();
        if (!PENDING_PAYMENT.equals(status) && !TO_BE_CONFIRMED.equals(status)) {
            log.warn("支付回调订单状态异常，订单号: {}，当前状态: {}", outTradeNo, status);
            return;
        }

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
        OrderVO orderVO = orderReadConvert.toVO(order);
        orderVO.setOrderDetailList(orderReadConvert.toDetailVOList(details));
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
        dto.setUserId(BaseContext.getCurrentId());

        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(dto);

        // 统一空页安全返回
        if (page == null) {
            return new PageResult(0L, Collections.emptyList());
        }
        if (page.isEmpty()) {
            // 当前页无数据（可能越界），返回 total 和空 list
            return new PageResult(page.getTotal(), Collections.emptyList());
        }

        // 1) 收集当前页订单 id（distinct 防御）
        List<Long> orderIds = page.stream()
                .map(Orders::getId)
                .distinct()
                .collect(Collectors.toList());

        // 2) 一次性查出这些订单的所有明细
        List<OrderDetail> allDetails = orderDetailMapper.listByOrderIds(orderIds);

        // 3) 按 orderId 分组
        Map<Long, List<OrderDetail>> detailMap = allDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 4) 组装 VO（预分配容量）
        List<OrderVO> orderVOList = new ArrayList<>(page.size());
        for (Orders order : page) {
            OrderVO orderVO = orderReadConvert.toVO(order);

            List<OrderDetail> details = detailMap.getOrDefault(order.getId(), Collections.emptyList());
            orderVO.setOrderDetailList(orderReadConvert.toDetailVOList(details));

            orderVOList.add(orderVO);
        }

        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * 用户取消订单
     * 允许状态：PENDING_PAYMENT(1) 或 TO_BE_CONFIRMED(2) -> CANCELLED(6)
     *
     * @param id
     */
    @Override
    public void cancel(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("订单id不能为空");
        }

        // 先校验是否是当前用户的订单
        Long userId = BaseContext.getCurrentId();
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        LocalDateTime cancelTime = LocalDateTime.now();

        // 尝试从待付款状态取消
        int rows = orderMapper.cancelIfMatch(id, PENDING_PAYMENT, USER_CANCEL, cancelTime);
        if (rows == 1) {
            return; // 待付款状态取消成功
        }

        // 尝试从待接单状态取消
        rows = orderMapper.cancelIfMatch(id, TO_BE_CONFIRMED, USER_CANCEL, cancelTime);
        if (rows == 1) {
            // 待接单状态（已支付）取消成功，需要退款
            log.info("订单{}需要退款，已支付金额：{}", order.getNumber(), order.getAmount());
            // TODO: 调用微信退款接口
            return;
        }

        // 两种状态都不匹配，抛状态错误
        throw new OrderBusinessException(ORDER_STATUS_ERROR);
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    @Transactional
    public void repetition(Long id) {
        Orders order = orderMapper.getById(id);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        Long userId = BaseContext.getCurrentId();
        if (!userId.equals(order.getUserId())) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        List<OrderDetail> details = orderDetailMapper.getByOrderId(id);
        if (details == null || details.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_ITEM_NOT_FOUND); // 或者抛异常，看你业务
        }

        LocalDateTime now = LocalDateTime.now();
        List<ShoppingCart> carts = details.stream()
                .map(d -> {
                    ShoppingCart c = new ShoppingCart();
                    c.setUserId(userId);
                    c.setDishId(d.getDishId());
                    c.setSetmealId(d.getSetmealId());
                    c.setDishFlavor(d.getDishFlavor());
                    c.setNumber(d.getNumber());
                    c.setCreateTime(now);

                    c.setName(d.getName());
                    c.setImage(d.getImage());
                    c.setAmount(d.getAmount());

                    return c;
                })
                .collect(Collectors.toList());

        shoppingCartMapper.insertBatch(carts);
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

        // 统一空页安全返回
        if (page == null) {
            return new PageResult(0L, Collections.emptyList());
        }
        if (page.isEmpty()) {
            // 当前页无数据（可能越界），返回 total 和空 list
            return new PageResult(page.getTotal(), Collections.emptyList());
        }

        // 1) 收集当前页订单 id（distinct 防御）
        List<Long> orderIds = page.stream()
                .map(Orders::getId)
                .distinct()
                .collect(Collectors.toList());

        // 2) 一次性查出这些订单的所有明细
        List<OrderDetail> allDetails = orderDetailMapper.listByOrderIds(orderIds);

        // 3) 按 orderId 分组
        Map<Long, List<OrderDetail>> detailMap = allDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 4) 组装 VO（预分配容量）
        List<OrderVO> orderVOList = new ArrayList<>(page.size());
        for (Orders order : page) {
            OrderVO orderVO = orderReadConvert.toVO(order);

            List<OrderDetail> details = detailMap.getOrDefault(order.getId(), Collections.emptyList());
            String orderDishes = details.stream()
                    .map(d -> d.getName() + "*" + d.getNumber())
                    .collect(Collectors.joining("；"));

            orderVO.setOrderDishes(orderDishes);
            orderVOList.add(orderVO);
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
     * @param orderId
     * @return
     */
    @Override
    public OrderVO getDetailAdmin(Long orderId) {
        // 查询订单
        Orders order = orderMapper.getById(orderId);
        if (order == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }

        // 查询订单明细
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);

        // 组装VO
        OrderVO orderVO = orderReadConvert.toVO(order);

        // 转换订单明细为 OrderDetailVO
        List<OrderDetailVO> detailVOList = details.stream()
                .map(orderReadConvert::toDetailVO)
                .collect(Collectors.toList());
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
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("订单参数不能为空");
        }

        Long id = dto.getId();

        int rows = orderMapper.updateStatusIfMatch(
                id,
                TO_BE_CONFIRMED,
                CONFIRMED);

        if (rows == 1) {
            return; // 接单成功
        }

        // rows==0：要么订单不存在，要么状态不允许（已被处理/不是待接单）
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        throw new OrderBusinessException(ORDER_STATUS_ERROR);
    }

    /**
     * 拒单
     * 状态流转：TO_BE_CONFIRMED(2) -> CANCELLED(6)
     *
     * @param dto
     */
    @Override
    public void rejection(OrdersRejectionDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("订单参数不能为空");
        }

        Long id = dto.getId();
        LocalDateTime cancelTime = LocalDateTime.now();

        int rows = orderMapper.rejectIfMatch(id, dto.getRejectionReason(), cancelTime);
        if (rows == 1) {
            // 拒单成功，查询订单判断是否需要退款
            Orders orderDB = orderMapper.getById(id);
            if (orderDB != null && PAID.equals(orderDB.getPayStatus())) {
                log.info("订单{}拒单，需要退款，已支付金额：{}", orderDB.getNumber(), orderDB.getAmount());
                // TODO: 调用微信退款接口
            }
            return;
        }

        // rows==0：订单不存在或状态不匹配
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        throw new OrderBusinessException(ORDER_STATUS_ERROR);
    }

    /**
     * 管理端取消订单（强制取消，排除已完成/已取消）
     * 允许状态：除 COMPLETED(5) 和 CANCELLED(6) 外 -> CANCELLED(6)
     *
     * @param dto
     */
    @Override
    public void cancelAdmin(OrdersCancelDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("订单参数不能为空");
        }

        Long id = dto.getId();
        LocalDateTime cancelTime = LocalDateTime.now();

        int rows = orderMapper.cancelAdminIfNotCompleted(id, dto.getCancelReason(), cancelTime);
        if (rows == 1) {
            // 取消成功，查询订单判断是否需要退款
            Orders orderDB = orderMapper.getById(id);
            if (orderDB != null && PAID.equals(orderDB.getPayStatus())) {
                log.info("管理员取消订单{}，需要退款，已支付金额：{}", orderDB.getNumber(), orderDB.getAmount());
                // TODO: 调用微信退款接口
            }
            return;
        }

        // rows==0：订单不存在或状态为已完成/已取消
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        throw new OrderBusinessException(ORDER_STATUS_ERROR);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("订单id不能为空");
        }

        int rows = orderMapper.updateStatusIfMatch(id, CONFIRMED, DELIVERY_IN_PROGRESS);
        if (rows == 1) {
            return;
        }

        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        throw new OrderBusinessException(ORDER_STATUS_ERROR);
    }

    /**
     * 完成订单
     * 状态流转：DELIVERY_IN_PROGRESS(4) -> COMPLETED(5)
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("订单id不能为空");
        }

        int rows = orderMapper.completeIfMatch(id, LocalDateTime.now());
        if (rows == 1) {
            return; // 完成成功
        }

        // rows==0：订单不存在或状态不匹配
        Orders orderDB = orderMapper.getById(id);
        if (orderDB == null) {
            throw new OrderBusinessException(ORDER_NOT_FOUND);
        }
        throw new OrderBusinessException(ORDER_STATUS_ERROR);
    }
}
