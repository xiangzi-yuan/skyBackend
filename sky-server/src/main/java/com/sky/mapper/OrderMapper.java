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

}
