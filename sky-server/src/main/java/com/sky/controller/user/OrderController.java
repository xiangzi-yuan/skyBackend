package com.sky.controller.user;

import com.sky.dto.order.OrdersPageQueryDTO;
import com.sky.dto.order.OrdersPaymentDTO;
import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.order.OrderPaymentVO;
import com.sky.vo.order.OrderSubmitVO;
import com.sky.vo.order.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端-订单接口")
@Slf4j
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 用户下单
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO dto) {
        log.info("提交金额为{}的订单", dto.getAmount());
        return Result.success(orderService.submit(dto));
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getDetail(@PathVariable Long id) {
        log.info("查询订单详情：{}", id);
        return Result.success(orderService.getDetail(id));
    }

    /**
     * 历史订单查询
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO dto) {
        log.info("历史订单查询：page={}, pageSize={}, status={}", dto.getPage(), dto.getPageSize(), dto.getStatus());
        return Result.success(orderService.historyOrders(dto));
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result<String> cancel(@PathVariable Long id) {
        log.info("取消订单：{}", id);
        orderService.cancel(id);
        return Result.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result<String> repetition(@PathVariable Long id) {
        log.info("再来一单：{}", id);
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 催单
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("催单")
    public Result<String> reminder(@PathVariable Long id) {
        log.info("催单：{}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
