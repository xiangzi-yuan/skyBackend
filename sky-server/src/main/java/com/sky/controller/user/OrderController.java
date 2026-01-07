package com.sky.controller.user;

import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
