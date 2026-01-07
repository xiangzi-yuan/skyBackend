package com.sky.controller.user;

import com.sky.dto.order.OrdersPaymentDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("根据ID查询套餐详情")
    Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO dto) {
        log.info("提交金额为{}的订单", dto.getAmount());
        return Result.success(orderService.submit(dto));
    }

    @PostMapping("/payment")
    @ApiOperation("订单支付")
    Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO dto) {
        log.info("订单支付：{}", dto.getOrderNumber());
        return Result.success(orderService.payment(dto));
    }

    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    Result<OrderVO> getDetail(@RequestBody OrdersPaymentDTO dto) {
        log.info("订单支付：{}", dto.getOrderNumber());
        return null;
    }



}
