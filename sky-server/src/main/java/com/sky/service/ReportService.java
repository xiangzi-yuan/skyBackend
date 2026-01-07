package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.order.OrderReportVO;
import com.sky.vo.setmeal.SalesTop10ReportVO;
import com.sky.vo.user.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * 数据统计服务接口
 */
public interface ReportService {

    /**
     * 营业额统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名Top10
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    /**
     * 导出运营数据Excel报表
     *
     * @param response
     */
    void exportBusinessReport(HttpServletResponse response);
}
