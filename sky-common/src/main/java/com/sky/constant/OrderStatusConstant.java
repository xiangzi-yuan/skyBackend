package com.sky.constant;

/**
 * 订单状态常量
 * 订单状态：1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款
 */
public final class OrderStatusConstant {

    private OrderStatusConstant() {}

    public static final Integer PENDING_PAYMENT = 1;        // 待付款
    public static final Integer TO_BE_CONFIRMED = 2;        // 待接单
    public static final Integer CONFIRMED = 3;              // 已接单
    public static final Integer DELIVERY_IN_PROGRESS = 4;   // 派送中
    public static final Integer COMPLETED = 5;              // 已完成
    public static final Integer CANCELLED = 6;              // 已取消
    public static final Integer REFUND = 7;                 // 退款


}

