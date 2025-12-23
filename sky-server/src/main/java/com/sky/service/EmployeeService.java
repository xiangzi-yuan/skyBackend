package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeDetailVO;
import com.sky.vo.EmployeeLoginVO;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeCreateDTO employeeCreateDTO);

    void changeEmployee(EmployeeUpdateDTO dto);

    void changePassword(PasswordEditDTO passwordEditDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    EmployeeDetailVO getById(Long id);

    void updateStatus(Long id, Integer status);
}
