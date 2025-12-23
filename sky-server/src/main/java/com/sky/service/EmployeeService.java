package com.sky.service;

import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeDetailVO;

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
     * 修改员工接口
     * @param dto
     */
    void changeEmployee(EmployeeUpdateDTO dto);
    /**
     * 修改密码接口方法
     * @param passwordEditDTO
     */
    void changePassword(PasswordEditDTO passwordEditDTO);

    /**
     * 查询员工
     * @param employeePageQueryDTO
     */
    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    EmployeeDetailVO getById(Long id);

    void updateStatus(Long id, Integer status);



}
