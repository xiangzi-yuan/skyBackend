package com.sky.converter;

import com.sky.dto.EmployeeCreateDTO;
import com.sky.dto.EmployeeUpdateDTO;
import com.sky.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeWriteConvert {

    /**
     * 新增员工：DTO -> Entity
     * 说明：系统字段（password/status/createTime/...）由 Service 填充，避免“信任前端”
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "pwdChanged", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    Employee fromCreateDTO(EmployeeCreateDTO dto);

    /**
     * 修改员工：DTO -> Entity（局部更新）
     * 规则：null 不覆盖（只拷贝非 null 字段）
     * 注意：id/updateUser 等系统字段由 Service 设置
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "status", ignore = true),
            @Mapping(target = "pwdChanged", ignore = true),
            @Mapping(target = "createTime", ignore = true),
            @Mapping(target = "updateTime", ignore = true),
            @Mapping(target = "createUser", ignore = true),
            @Mapping(target = "updateUser", ignore = true)
    })
    void mergeUpdate(EmployeeUpdateDTO dto, @MappingTarget Employee employee);
}
