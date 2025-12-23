package com.sky.converter;

import com.sky.readmodel.employee.EmployeeDetailRM;
import com.sky.readmodel.employee.EmployeePageRM;
import com.sky.vo.EmployeeDetailVO;
import com.sky.vo.EmployeePageVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeReadConvert {

    EmployeeDetailVO toDetailVO(EmployeeDetailRM rm);

    EmployeePageVO toPageVO(EmployeePageRM rm);
}
