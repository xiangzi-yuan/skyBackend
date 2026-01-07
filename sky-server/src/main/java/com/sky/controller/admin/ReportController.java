package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.order.OrderReportVO;
import com.sky.vo.setmeal.SalesTop10ReportVO;
import com.sky.vo.user.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 数据统计接口
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 营业额统计
     */
    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计接口")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("营业额统计：begin={}, end={}", begin, end);
        return Result.success(reportService.getTurnoverStatistics(begin, end));
    }

    /**
     * 用户统计
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户统计接口")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("用户统计：begin={}, end={}", begin, end);
        return Result.success(reportService.getUserStatistics(begin, end));
    }

    /**
     * 订单统计
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result<OrderReportVO> ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("订单统计：begin={}, end={}", begin, end);
        return Result.success(reportService.getOrdersStatistics(begin, end));
    }

    /**
     * 销量排名Top10
     */
    @GetMapping("/top10")
    @ApiOperation("查询销量排名top10接口")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        log.info("销量排名Top10：begin={}, end={}", begin, end);
        return Result.success(reportService.getSalesTop10(begin, end));
    }

    /**
     * 导出Excel报表
     */
    @GetMapping("/export")
    @ApiOperation("导出Excel报表接口")
    public void export(HttpServletResponse response) {
        log.info("导出Excel报表");
        reportService.exportBusinessReport(response);
    }
}
