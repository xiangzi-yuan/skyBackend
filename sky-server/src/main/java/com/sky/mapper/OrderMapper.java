package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.order.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

        void insert(Orders order);

        @Select("select * from orders where number = #{orderNumber} limit 1")
        Orders getByNumber(@Param("orderNumber") String orderNumber);

        /**
         * 修改订单信息
         * 
         * @param orders
         */
        void update(Orders orders);

        /**
         * 根据id查询订单
         * 
         * @param id
         * @return
         */
        @Select("select * from orders where id = #{id}")
        Orders getById(Long id);

        /**
         * 分页查询订单
         * 
         * @param dto
         * @return
         */
        Page<Orders> pageQuery(OrdersPageQueryDTO dto);

        /**
         * 根据状态和下单时间查询订单
         * 
         * @param status    订单状态
         * @param orderTime 下单时间（查询此时间之前的订单）
         * @return
         */
        @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
        List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status,
                        @Param("orderTime") LocalDateTime orderTime);

        /**
         * 根据状态统计订单数量
         * 
         * @param status 订单状态
         * @return
         */
        @Select("select count(id) from orders where status = #{status}")
        Integer countByStatus(@Param("status") Integer status);

        /**
         * 统计时间范围内指定状态的营业额
         * 
         * @param status 订单状态
         * @param begin  开始时间
         * @param end    结束时间
         * @return
         */
        Double sumAmountByStatusAndTime(@Param("status") Integer status,
                        @Param("begin") LocalDateTime begin,
                        @Param("end") LocalDateTime end);

        /**
         * 统计时间范围内指定状态的订单数量
         * 
         * @param status 订单状态（为null时统计所有状态）
         * @param begin  开始时间
         * @param end    结束时间
         * @return
         */
        Integer countByStatusAndTime(@Param("status") Integer status,
                        @Param("begin") LocalDateTime begin,
                        @Param("end") LocalDateTime end);

        /**
         * 条件更新订单状态（原子操作，并发安全）
         * 只有当前状态与 fromStatus 匹配时才更新为 toStatus
         *
         * @param id         订单ID
         * @param fromStatus 期望的当前状态
         * @param toStatus   要更新到的目标状态
         * @return 影响行数（1=成功，0=状态不匹配或订单不存在）
         */
        int updateStatusIfMatch(@Param("id") Long id,
                        @Param("fromStatus") Integer fromStatus,
                        @Param("toStatus") Integer toStatus);

        /**
         * CAS: 完成订单（DELIVERY_IN_PROGRESS -> COMPLETED）并设置 deliveryTime
         *
         * @param id           订单ID
         * @param deliveryTime 送达时间
         * @return 影响行数（1=成功，0=状态不匹配或订单不存在）
         */
        int completeIfMatch(@Param("id") Long id,
                        @Param("deliveryTime") LocalDateTime deliveryTime);

        /**
         * CAS: 拒单（TO_BE_CONFIRMED -> CANCELLED）并设置 rejectionReason + cancelTime
         *
         * @param id              订单ID
         * @param rejectionReason 拒单原因
         * @param cancelTime      取消时间
         * @return 影响行数
         */
        int rejectIfMatch(@Param("id") Long id,
                        @Param("rejectionReason") String rejectionReason,
                        @Param("cancelTime") LocalDateTime cancelTime);

        /**
         * CAS: 取消订单（指定状态 -> CANCELLED）并设置 cancelReason + cancelTime
         *
         * @param id           订单ID
         * @param fromStatus   期望的当前状态
         * @param cancelReason 取消原因
         * @param cancelTime   取消时间
         * @return 影响行数
         */
        int cancelIfMatch(@Param("id") Long id,
                        @Param("fromStatus") Integer fromStatus,
                        @Param("cancelReason") String cancelReason,
                        @Param("cancelTime") LocalDateTime cancelTime);

        /**
         * CAS: 管理员强制取消（排除已完成/已取消的订单）
         *
         * @param id           订单ID
         * @param cancelReason 取消原因
         * @param cancelTime   取消时间
         * @return 影响行数
         */
        int cancelAdminIfNotCompleted(@Param("id") Long id,
                        @Param("cancelReason") String cancelReason,
                        @Param("cancelTime") LocalDateTime cancelTime);

        /**
         * 按日期分组统计营业额（用于批量报表导出）
         *
         * @param status 订单状态
         * @param begin  开始时间
         * @param end    结束时间
         * @return 每日营业额列表，每个Map包含 date 和 turnover
         */
        List<java.util.Map<String, Object>> sumAmountGroupByDate(@Param("status") Integer status,
                        @Param("begin") LocalDateTime begin,
                        @Param("end") LocalDateTime end);

        /**
         * 按日期分组统计订单数量（用于批量报表导出）
         *
         * @param status 订单状态（为null时统计所有状态）
         * @param begin  开始时间
         * @param end    结束时间
         * @return 每日订单数列表，每个Map包含 date 和 count
         */
        List<java.util.Map<String, Object>> countGroupByDate(@Param("status") Integer status,
                        @Param("begin") LocalDateTime begin,
                        @Param("end") LocalDateTime end);

}
