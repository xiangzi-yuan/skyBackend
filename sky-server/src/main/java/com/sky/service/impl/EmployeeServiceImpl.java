package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.PwdChangedConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeCreateDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder; // 哈希校验

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对

        // password：前端传来的明文
        // employee.getPassword()：数据库里存的 bcrypt 哈希串（例如 $2b$12$...）
        if (!passwordEncoder.matches(password, employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * 逻辑改为初始默认密码加强制首次改密码
     * @param employeeCreateDTO
     */
    @Override
    public void save(EmployeeCreateDTO employeeCreateDTO) {
        // 对象属性拷贝
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeCreateDTO,employee);
        // 补全剩余属性
        // 状态
        employee.setStatus(StatusConstant.ENABLE);
        // 设置密码
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));


        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        Long id = BaseContext.getCurrentId();
        employee.setCreateUser(id);
        employee.setUpdateUser(id);
        // 是否修改密码
        employee.setPwdChanged(PwdChangedConstant.PWD);

        employeeMapper.insert(employee);
    }

    @Override
    public void changePassword(PasswordEditDTO passwordEditDTO){

        // 防止越权：只能改当前登录用户的密码
        Long empId = BaseContext.getCurrentId();
        Employee employee = employeeMapper.getById(empId);

        if (employee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        // 校验原密码
        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), employee.getPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }
        // mapper设置新密码
        String newHash = passwordEncoder.encode(passwordEditDTO.getNewPassword());
        employeeMapper.updatePasswordAndMarkChanged(employee.getId(),
                newHash,
                LocalDateTime.now(),
                empId);

    }

}
