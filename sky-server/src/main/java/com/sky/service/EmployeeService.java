package com.sky.service;

import com.sky.dto.*;
import com.sky.dto.employee.EmployeeCreateDTO;
import com.sky.dto.employee.EmployeeLoginDTO;
import com.sky.dto.employee.EmployeePageQueryDTO;
import com.sky.dto.employee.EmployeeUpdateDTO;
import com.sky.result.PageResult;
import com.sky.vo.employee.EmployeeDetailVO;
import com.sky.vo.employee.EmployeeLoginVO;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeCreateDTO employeeCreateDTO);

    void changeEmployee(EmployeeUpdateDTO dto);

    void changePassword(PasswordEditDTO passwordEditDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    EmployeeDetailVO getById(Long id);

    void updateStatus(Long id, Integer status);
}
