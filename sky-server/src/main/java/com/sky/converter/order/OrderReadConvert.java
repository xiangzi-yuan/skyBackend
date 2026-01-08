package com.sky.converter.order;

import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.vo.order.OrderDetailVO;
import com.sky.vo.order.OrderVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

// 写了列表映射，MapStruct 会自动生成列表内部每个元素的映射代码；但不会自动生成一个可被你调用的“单个元素映射方法签名”。
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderReadConvert {

    OrderVO toVO(Orders order);

    OrderDetailVO toDetailVO(OrderDetail detail); // 可忽略

    List<OrderDetailVO> toDetailVOList(List<OrderDetail> details);
}
