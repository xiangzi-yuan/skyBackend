package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.order.OrderReportVO;
import com.sky.vo.setmeal.SalesTop10ReportVO;
import com.sky.vo.user.UserReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sky.constant.OrderStatusConstant.COMPLETED;

/**
 * 数据统计服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 查询每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Double turnover = orderMapper.sumAmountByStatusAndTime(COMPLETED, beginTime, endTime);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 查询每天的新增用户数和总用户数
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 新增用户数
            Integer newUsers = userMapper.countByCreateTime(beginTime, endTime);
            newUsers = newUsers == null ? 0 : newUsers;
            newUserList.add(newUsers);

            // 总用户数（截止到当天结束）
            Integer totalUsers = userMapper.countByCreateTime(null, endTime);
            totalUsers = totalUsers == null ? 0 : totalUsers;
            totalUserList.add(totalUsers);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        // 构建日期列表
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 查询每天的订单数和有效订单数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 每日订单数
            Integer orderCount = orderMapper.countByStatusAndTime(null, beginTime, endTime);
            orderCount = orderCount == null ? 0 : orderCount;
            orderCountList.add(orderCount);

            // 每日有效订单数
            Integer validOrderCount = orderMapper.countByStatusAndTime(COMPLETED, beginTime, endTime);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            validOrderCountList.add(validOrderCount);
        }

        // 计算总数
        Integer totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);
        Integer validOrderCount = validOrderCountList.stream().reduce(0, Integer::sum);

        // 订单完成率
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 销量排名Top10
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderDetailMapper.getSalesTop10(beginTime, endTime);

        String nameList = salesTop10.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));

        String numberList = salesTop10.stream()
                .map(dto -> dto.getNumber().toString())
                .collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据Excel报表
     *
     * @param response
     */
    @Override
    public void exportBusinessReport(HttpServletResponse response) {
        // 查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        // 获取运营数据概览
        BusinessDataVO businessData = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 读取Excel模板
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            // 基于模板创建Excel
            XSSFWorkbook excel = new XSSFWorkbook(is);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            // 填充概览数据
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + " 至 " + dateEnd);

            XSSFRow row3 = sheet.getRow(3);
            row3.getCell(2).setCellValue(businessData.getTurnover());
            row3.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row3.getCell(6).setCellValue(businessData.getNewUsers());

            XSSFRow row4 = sheet.getRow(4);
            row4.getCell(2).setCellValue(businessData.getValidOrderCount());
            row4.getCell(4).setCellValue(businessData.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO dayData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dayData.getTurnover());
                row.getCell(3).setCellValue(dayData.getValidOrderCount());
                row.getCell(4).setCellValue(dayData.getOrderCompletionRate());
                row.getCell(5).setCellValue(dayData.getUnitPrice());
                row.getCell(6).setCellValue(dayData.getNewUsers());
            }

            // 输出Excel到响应
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();
            excel.close();

        } catch (IOException e) {
            log.error("导出Excel报表失败", e);
        }
    }
}
