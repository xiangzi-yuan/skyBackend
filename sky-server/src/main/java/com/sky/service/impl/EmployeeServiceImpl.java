package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.auth.model.EmployeeAuthInfo;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.PwdChangedConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeDetailVO;
import com.sky.vo.EmployeePageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
     *
     * @param employeeCreateDTO
     */
    @Override
    public void save( EmployeeCreateDTO employeeCreateDTO) {
        // 对象属性拷贝
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeCreateDTO, employee);
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
        employee.setPwdChanged(PwdChangedConstant.NOT_CHANGED);

        employeeMapper.insert(employee);
    }

    @Override
    public void changeEmployee(EmployeeUpdateDTO dto) {
        Long currentId = BaseContext.getCurrentId();
        Employee emp = Employee.builder()
                .id(dto.getId())
                .name(dto.getName())
                .username(dto.getUsername())
                .phone(dto.getPhone())
                .sex(dto.getSex())
                .idNumber(dto.getIdNumber())
                .updateUser(currentId)
                .build();

        employeeMapper.update(emp);
    }

    @Override
    public void changePassword(PasswordEditDTO passwordEditDTO) {

        // 只能改当前登录人
        Long empId = BaseContext.getCurrentId();

        // 改：用鉴权信息查询（含 password/status/role/pwdChanged）
        EmployeeAuthInfo auth = employeeMapper.getAuthInfoById(empId);
        if (auth == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 可选：账号禁用直接拒绝（按你项目常量）
        if (auth.getStatus() != null && auth.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 校验旧密码：raw=用户输入，encoded=数据库hash
        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), auth.getPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 可选：新旧不能相同
        if (passwordEditDTO.getNewPassword() != null
                && passwordEditDTO.getNewPassword().equals(passwordEditDTO.getOldPassword())) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }

        // 入库必须是 hash
        String newHash = passwordEncoder.encode(passwordEditDTO.getNewPassword());

        // 改：更新用 empId，别用 employee.getId()
        employeeMapper.updatePasswordAndMarkChanged(empId, newHash, LocalDateTime.now(), empId);
    }


    @Override
    public PageResult pageQuery(EmployeePageQueryDTO dto) {
        // 开始分页查询
        PageHelper.startPage(dto.getPage(), dto.getPageSize()); // 插件
        Page<EmployeePageVO> page = employeeMapper.pageQuery(dto);

        long total = page.getTotal();
        List<EmployeePageVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public EmployeeDetailVO getById(Long id) {
        return employeeMapper.getDetailById(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 更新状态
        Employee emp = Employee.builder()
                .status(status)
                .id(id)
                .updateUser(BaseContext.getCurrentId())
                .build();

        employeeMapper.updateStatus(emp);
    }



}
