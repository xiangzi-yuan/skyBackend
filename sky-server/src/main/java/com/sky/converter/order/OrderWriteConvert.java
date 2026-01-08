package com.sky.converter.order;

import com.sky.dto.order.OrdersSubmitDTO;
import com.sky.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderWriteConvert {




    /**
     * int：永远有值，默认 0，无法表达 null/未提供
     * Integer：可以为 null，能表达“未提供/未知/不适用”
     * 所以：统一成 Integer 的代价是：你必须处理 null；
     * 统一成 int 的代价是：你丢失‘未提供’语义，并且可能把 0 当成合法值误写入库。
     * 所以dto设计为 Integer, 而 entity 设计为 int
     * @param dto
     * @return
     */
    // 防止自动装箱NPE
    @Mapping(target = "packAmount", expression = "java(dto.getPackAmount() == null ? 0 : dto.getPackAmount())")
    @Mapping(target = "tablewareNumber", expression = "java(dto.getTablewareNumber() == null ? 0 : dto.getTablewareNumber())")
    @Mapping(target = "amount", ignore = true)
    Orders fromCreateDTO(OrdersSubmitDTO dto);



}
