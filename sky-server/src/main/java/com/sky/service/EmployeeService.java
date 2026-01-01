package com.sky.service;

import com.sky.dto.PasswordEditDTO;
import com.sky.dto.employee.EmployeeCreateDTO;
import com.sky.dto.employee.EmployeeLoginDTO;
import com.sky.dto.employee.EmployeePageQueryDTO;
import com.sky.dto.employee.EmployeeRoleUpgradeDTO;
import com.sky.dto.employee.EmployeeUpdateDTO;
import com.sky.result.PageResult;
import com.sky.vo.employee.EmployeeDetailVO;
import com.sky.vo.employee.EmployeeLoginVO;
import com.sky.vo.employee.EmployeeRoleVO;
import com.sky.vo.employee.RoleLevelOptionVO;

import java.util.List;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeCreateDTO employeeCreateDTO);

    void changeEmployee(EmployeeUpdateDTO dto);

    void changePassword(PasswordEditDTO passwordEditDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    EmployeeDetailVO getById(Long id);

    void updateStatus(Long id, Integer status);

    /**
     * 员工权限提升（仅SUPER可用）
     */
    void upgradeRole(EmployeeRoleUpgradeDTO dto);

    /**
     * 获取员工权限信息
     */
    EmployeeRoleVO getRoleInfo(Long employeeId);

    /**
     * 获取所有权限等级选项
     */
    List<RoleLevelOptionVO> getRoleLevelOptions();
}
