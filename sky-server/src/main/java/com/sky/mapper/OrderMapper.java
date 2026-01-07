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


    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

}
