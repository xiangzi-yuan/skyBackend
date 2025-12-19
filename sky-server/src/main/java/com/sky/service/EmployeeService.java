package com.sky.service;

import com.sky.dto.EmployeeCreateDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工接口方法
     * @param employeeCreateDTO
     */
    void save(EmployeeCreateDTO employeeCreateDTO);

    /**
     * 修改密码接口方法
     * @param passwordEditDTO
     */
    void changePassword(PasswordEditDTO passwordEditDTO);
}
