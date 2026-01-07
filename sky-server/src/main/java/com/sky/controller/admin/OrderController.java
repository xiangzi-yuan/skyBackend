package com.sky.controller.admin;

import com.sky.dto.order.OrdersCancelDTO;
import com.sky.dto.order.OrdersConfirmDTO;
import com.sky.dto.order.OrdersPageQueryDTO;
import com.sky.dto.order.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.order.OrderStatisticsVO;
import com.sky.vo.order.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端订单管理接口
 */
@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单管理接口")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 订单搜索
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO dto) {
        log.info("订单搜索：{}", dto);
        PageResult pageResult = orderService.conditionSearch(dto);
        return Result.success(pageResult);
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        OrderStatisticsVO vo = orderService.statistics();
        return Result.success(vo);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情，id={}", id);
        OrderVO orderVO = orderService.getDetailAdmin(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO dto) {
        log.info("接单：{}", dto);
        orderService.confirm(dto);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO dto) {
        log.info("拒单：{}", dto);
        orderService.rejection(dto);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<String> cancel(@RequestBody OrdersCancelDTO dto) {
        log.info("取消订单：{}", dto);
        orderService.cancelAdmin(dto);
        return Result.success();
    }

    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result<String> delivery(@PathVariable Long id) {
        log.info("派送订单：{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result<String> complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        orderService.complete(id);
        return Result.success();
    }
}
