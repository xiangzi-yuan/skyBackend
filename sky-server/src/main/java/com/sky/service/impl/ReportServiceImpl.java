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
     * 优化：使用批量查询替代逐日查询，减少数据库访问次数
     *
     * @param response
     */
    @Override
    public void exportBusinessReport(HttpServletResponse response) {
        // 查询最近30天的运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime beginTime = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateEnd, LocalTime.MAX);

        // 获取运营数据概览
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);

        // ======== 批量查询每日数据（优化核心：4次查询替代原来的120+次） ========
        // 1. 批量查询每日营业额（已完成订单）
        List<java.util.Map<String, Object>> turnoverList = orderMapper.sumAmountGroupByDate(COMPLETED, beginTime,
                endTime);
        java.util.Map<LocalDate, Double> turnoverMap = new java.util.HashMap<>();
        for (java.util.Map<String, Object> row : turnoverList) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Double turnover = row.get("turnover") != null ? ((Number) row.get("turnover")).doubleValue() : 0.0;
            turnoverMap.put(date, turnover);
        }

        // 2. 批量查询每日有效订单数（已完成订单）
        List<java.util.Map<String, Object>> validOrderList = orderMapper.countGroupByDate(COMPLETED, beginTime,
                endTime);
        java.util.Map<LocalDate, Integer> validOrderMap = new java.util.HashMap<>();
        for (java.util.Map<String, Object> row : validOrderList) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Integer count = row.get("count") != null ? ((Number) row.get("count")).intValue() : 0;
            validOrderMap.put(date, count);
        }

        // 3. 批量查询每日总订单数
        List<java.util.Map<String, Object>> totalOrderList = orderMapper.countGroupByDate(null, beginTime, endTime);
        java.util.Map<LocalDate, Integer> totalOrderMap = new java.util.HashMap<>();
        for (java.util.Map<String, Object> row : totalOrderList) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Integer count = row.get("count") != null ? ((Number) row.get("count")).intValue() : 0;
            totalOrderMap.put(date, count);
        }

        // 4. 批量查询每日新增用户数
        List<java.util.Map<String, Object>> newUserList = userMapper.countGroupByCreateDate(beginTime, endTime);
        java.util.Map<LocalDate, Integer> newUserMap = new java.util.HashMap<>();
        for (java.util.Map<String, Object> row : newUserList) {
            LocalDate date = ((java.sql.Date) row.get("date")).toLocalDate();
            Integer count = row.get("count") != null ? ((Number) row.get("count")).intValue() : 0;
            newUserMap.put(date, count);
        }

        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "运营数据报表_" + dateBegin + "_" + dateEnd + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" +
                    java.net.URLEncoder.encode(fileName, "UTF-8"));

            // 动态创建Excel工作簿
            XSSFWorkbook excel = new XSSFWorkbook();
            XSSFSheet sheet = excel.createSheet("运营数据报表");

            // 设置列宽
            sheet.setColumnWidth(0, 5000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 4000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 4000);

            // 创建标题行
            XSSFRow row0 = sheet.createRow(0);
            row0.createCell(0).setCellValue("运营数据报表");

            // 时间范围行
            XSSFRow row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("时间：" + dateBegin + " 至 " + dateEnd);

            // 概览标题行
            XSSFRow row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("【数据概览】");

            // 概览数据 - 第一行
            XSSFRow row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("营业额");
            row3.createCell(1).setCellValue(businessData.getTurnover() != null ? businessData.getTurnover() : 0);
            row3.createCell(2).setCellValue("订单完成率");
            row3.createCell(3).setCellValue(
                    businessData.getOrderCompletionRate() != null ? businessData.getOrderCompletionRate() : 0);
            row3.createCell(4).setCellValue("新增用户数");
            row3.createCell(5).setCellValue(businessData.getNewUsers() != null ? businessData.getNewUsers() : 0);

            // 概览数据 - 第二行
            XSSFRow row4 = sheet.createRow(4);
            row4.createCell(0).setCellValue("有效订单数");
            row4.createCell(1)
                    .setCellValue(businessData.getValidOrderCount() != null ? businessData.getValidOrderCount() : 0);
            row4.createCell(2).setCellValue("平均客单价");
            row4.createCell(3).setCellValue(businessData.getUnitPrice() != null ? businessData.getUnitPrice() : 0);

            // 明细数据标题
            XSSFRow row6 = sheet.createRow(6);
            row6.createCell(0).setCellValue("【明细数据】");

            // 明细表头
            XSSFRow row7 = sheet.createRow(7);
            row7.createCell(0).setCellValue("日期");
            row7.createCell(1).setCellValue("营业额");
            row7.createCell(2).setCellValue("有效订单");
            row7.createCell(3).setCellValue("订单完成率");
            row7.createCell(4).setCellValue("平均客单价");
            row7.createCell(5).setCellValue("新增用户数");

            // 填充明细数据（从内存中的Map获取，不再逐日查询数据库）
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);

                // 从批量查询结果中获取数据
                Double turnover = turnoverMap.getOrDefault(date, 0.0);
                Integer validOrderCount = validOrderMap.getOrDefault(date, 0);
                Integer totalOrderCount = totalOrderMap.getOrDefault(date, 0);
                Integer newUsers = newUserMap.getOrDefault(date, 0);

                // 计算订单完成率
                Double orderCompletionRate = 0.0;
                if (totalOrderCount != 0) {
                    orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
                }

                // 计算平均客单价
                Double unitPrice = 0.0;
                if (validOrderCount != 0) {
                    unitPrice = turnover / validOrderCount;
                }

                XSSFRow row = sheet.createRow(8 + i);
                row.createCell(0).setCellValue(date.toString());
                row.createCell(1).setCellValue(turnover);
                row.createCell(2).setCellValue(validOrderCount);
                row.createCell(3).setCellValue(orderCompletionRate);
                row.createCell(4).setCellValue(unitPrice);
                row.createCell(5).setCellValue(newUsers);
            }

            // 输出Excel到响应
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.flush();
            out.close();
            excel.close();

        } catch (IOException e) {
            log.error("导出Excel报表失败", e);
            throw new RuntimeException("导出报表失败: " + e.getMessage());
        }
    }
}
