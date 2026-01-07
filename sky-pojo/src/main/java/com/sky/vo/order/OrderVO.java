package com.sky.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {
    private Long id;
    private String number;

    private Integer status;
    private Integer payStatus;
    private Integer payMethod;

    private Long userId;
    private Long addressBookId;

    private LocalDateTime orderTime;
    private LocalDateTime checkoutTime;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime cancelTime;
    private LocalDateTime deliveryTime;

    private Integer deliveryStatus;

    private Integer tablewareNumber;
    private Integer tablewareStatus;
    private String remark;

    private String userName;
    private String consignee;
    private String phone;
    private String address;

    private String cancelReason;
    private String rejectionReason;

    // 金额明细
    private Integer packAmount; // 打包费
    private BigDecimal amount; // 合计/应付

    // 订单菜品信息（字符串形式，如：宫保鸡丁*3；鱼香肉丝*2）
    private String orderDishes;

    private List<OrderDetailVO> orderDetailList;
}