package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.PwdChangedConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.converter.EmployeeReadConvert;
import com.sky.converter.EmployeeWriteConvert;
import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.readmodel.employee.EmployeeAuthInfo;
import com.sky.readmodel.employee.EmployeeDetailRM;
import com.sky.readmodel.employee.EmployeeLoginRM;
import com.sky.readmodel.employee.EmployeePageRM;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeDetailVO;
import com.sky.vo.EmployeeLoginVO;
import com.sky.vo.EmployeePageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private EmployeeReadConvert employeeReadConvert;

    @Autowired
    private EmployeeWriteConvert employeeWriteConvert;

    /**
     * 登录：Service 直接返回 VO（Controller 变薄）
     */
    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        EmployeeLoginRM rm = employeeMapper.getLoginInfoByUsername(username);
        if (rm == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (!passwordEncoder.matches(password, rm.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (rm.getStatus() != null && rm.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, rm.getId());
        claims.put(JwtClaimsConstant.EMP_ROLE, rm.getRole());

        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims
        );

        return EmployeeLoginVO.builder()
                .id(rm.getId())
                .userName(rm.getUsername())
                .name(rm.getName())
                .token(token)
                .needChangePassword(rm.getPwdChanged() == null || rm.getPwdChanged() == 0)
                .build();
    }

    /**
     * 新增员工：CreateDTO -> Entity 用 MapStruct
     * 系统字段在 Service 统一填充（零信任）
     */
    @Override
    public void save(EmployeeCreateDTO employeeCreateDTO) {
        Employee employee = employeeWriteConvert.fromCreateDTO(employeeCreateDTO);

        // 系统字段（不要信任前端）
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordEncoder.encode(PasswordConstant.DEFAULT_PASSWORD));
        employee.setPwdChanged(PwdChangedConstant.NOT_CHANGED);

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        Long currentId = BaseContext.getCurrentId();
        employee.setCreateUser(currentId);
        employee.setUpdateUser(currentId);

        employeeMapper.insert(employee);
    }

    /**
     * 修改员工：UpdateDTO -> Entity(局部更新) 用 MapStruct
     */
    @Override
    public void changeEmployee(EmployeeUpdateDTO dto) {
        Long currentId = BaseContext.getCurrentId();

        // 只构造“承载更新”的实体：必须带 id
        Employee emp = new Employee();
        emp.setId(dto.getId());

        // 合并非空字段（null 不覆盖）
        employeeWriteConvert.mergeUpdate(dto, emp);

        // 系统字段
        emp.setUpdateUser(currentId);

        employeeMapper.update(emp);
    }

    @Override
    public void changePassword(PasswordEditDTO passwordEditDTO) {
        Long empId = BaseContext.getCurrentId();

        if (passwordEditDTO.getOldPassword() == null || passwordEditDTO.getNewPassword() == null) {
            throw new IllegalArgumentException("oldPassword/newPassword is required");
        }

        EmployeeAuthInfo auth = employeeMapper.getAuthInfoById(empId);
        if (auth == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        if (auth.getStatus() != null && auth.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), auth.getPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        if (passwordEditDTO.getNewPassword().equals(passwordEditDTO.getOldPassword())) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }

        String newHash = passwordEncoder.encode(passwordEditDTO.getNewPassword());
        employeeMapper.updatePasswordAndMarkChanged(empId, newHash, LocalDateTime.now(), empId);
    }

    /**
     * 分页查询：Mapper -> RM；Service 用 MapStruct 转 VO
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());
        Page<EmployeePageRM> page = employeeMapper.pageQuery(dto);

        long total = page.getTotal();
        List<EmployeePageVO> records = page.getResult()
                .stream()
                .map(employeeReadConvert::toPageVO)
                .collect(Collectors.toList());

        return new PageResult(total, records);
    }

    /**
     * 根据id查询：Mapper -> RM；Service 用 MapStruct 转 VO
     */
    @Override
    public EmployeeDetailVO getById(Long id) {
        EmployeeDetailRM rm = employeeMapper.getDetailById(id);
        return employeeReadConvert.toDetailVO(rm);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Employee emp = new Employee();
        emp.setId(id);
        emp.setStatus(status);
        emp.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.updateStatus(emp);
    }
}
