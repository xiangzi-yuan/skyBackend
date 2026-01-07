package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface OrderMapper {


    void insert(Orders order);

    @Select("select * from orders where number = #{orderNumber} limit 1")
    Orders getByNumber(@Param("orderNumber") String orderNumber);

    @Update("""
            update orders
            set status = #{status},
                pay_status = #{payStatus},
                checkout_time = #{checkoutTime}
            where number = #{orderNumber}
            """)
    void updateStatusByNumber(@Param("status") Integer status,
                              @Param("payStatus") Integer payStatus,
                              @Param("checkoutTime") LocalDateTime checkoutTime,
                              @Param("orderNumber") String orderNumber);
}
