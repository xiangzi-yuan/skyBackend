package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    int insertBatch(List<OrderDetail> details);

    /**
     * 根据订单id查询订单明细
     * 
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 统计时间范围内销量排名前10的商品
     * 
     * @param begin 开始时间
     * @param end   结束时间
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

}
