package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.*;
import com.sky.context.BaseContext;
import com.sky.converter.EmployeeReadConvert;
import com.sky.converter.EmployeeWriteConvert;
import com.sky.dto.PasswordEditDTO;
import com.sky.dto.employee.*;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.BusinessException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.readmodel.employee.*;
import com.sky.result.PageResult;
import com.sky.role.RoleLevel;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.employee.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final EmployeeReadConvert employeeReadConvert;
    private final EmployeeWriteConvert employeeWriteConvert;
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
                .role(rm.getRole())
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


        employeeMapper.update(emp);
    }

    @Override
    public void changePassword(PasswordEditDTO passwordEditDTO) {
        Long empId = BaseContext.getCurrentId();

        // 校验密码规范
        if (passwordEditDTO.getOldPassword() == null || passwordEditDTO.getNewPassword() == null) {
            throw new IllegalArgumentException(MessageConstant.OLD_NEW_PASSWORD_REQUIRED);
        }

        // 校验权限
        EmployeeAuthInfo auth = employeeMapper.getAuthInfoById(empId);
        if (auth == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 校验状态
        if (auth.getStatus() != null && auth.getStatus() == StatusConstant.DISABLE) {
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 校验旧密码
        if (!passwordEncoder.matches(passwordEditDTO.getOldPassword(), auth.getPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 校验新密码
        if (passwordEditDTO.getNewPassword().equals(passwordEditDTO.getOldPassword())) {
            throw new IllegalArgumentException(MessageConstant.NEW_PASSWORD_SAME_AS_OLD);
        }

        String newHash = passwordEncoder.encode(passwordEditDTO.getNewPassword());
        Employee e = new Employee();
        e.setId(empId);
        e.setPassword(newHash);
        e.setPwdChanged(PwdChangedConstant.CHANGED); // 比如 1
        employeeMapper.updatePasswordAndMarkChanged(e);

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
        employeeMapper.updateStatus(emp);
    }

    /**
     * 提升员工权限（仅SUPER可用）
     */
    @Override
    public void upgradeRole(EmployeeRoleUpgradeDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        
        // 1. 验证目标员工是否存在
        EmployeeRoleRM targetEmployee = employeeMapper.getRoleInfoById(dto.getEmployeeId());
        if (targetEmployee == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 验证权限等级是否合法
        RoleLevel newRoleLevel = RoleLevel.fromValue(dto.getNewRole());
        if (newRoleLevel == null) {
            throw new IllegalArgumentException("无效的权限等级");
        }

        // 3. 不允许修改自己的权限
        if (dto.getEmployeeId().equals(currentUserId)) {
            throw new BusinessException("不能修改自己的权限");
        }

        // 4. 不允许降低SUPER用户的权限（防止误操作导致系统无管理员）
        if (targetEmployee.getRole() != null && 
            targetEmployee.getRole().equals(RoleLevel.SUPER.getValue()) &&
            !dto.getNewRole().equals(RoleLevel.SUPER.getValue())) {
            throw new BusinessException("不能降低超级管理员的权限");
        }

        // 5. 执行更新
        employeeMapper.updateRole(
            dto.getEmployeeId(), 
            dto.getNewRole(),
            LocalDateTime.now(),
            currentUserId
        );
    }

    /**
     * 获取员工权限信息
     */
    @Override
    public EmployeeRoleVO getRoleInfo(Long employeeId) {
        EmployeeRoleRM rm = employeeMapper.getRoleInfoById(employeeId);
        if (rm == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        RoleLevel roleLevel = RoleLevel.fromValue(rm.getRole());
        String roleName = roleLevel != null ? roleLevel.name() : "UNKNOWN";

        return EmployeeRoleVO.builder()
                .id(rm.getId())
                .name(rm.getName())
                .username(rm.getUsername())
                .role(rm.getRole())
                .roleName(roleName)
                .build();
    }

    /**
     * 获取所有可用的权限等级选项
     */
    @Override
    public List<RoleLevelOptionVO> getRoleLevelOptions() {
        List<RoleLevelOptionVO> options = new ArrayList<>();
        
        options.add(RoleLevelOptionVO.builder()
                .value(RoleLevel.STAFF.getValue())
                .label(RoleLevel.STAFF.name())
                .description("普通员工")
                .build());
        
        options.add(RoleLevelOptionVO.builder()
                .value(RoleLevel.MANAGER.getValue())
                .label(RoleLevel.MANAGER.name())
                .description("经理")
                .build());
        
        options.add(RoleLevelOptionVO.builder()
                .value(RoleLevel.SUPER.getValue())
                .label(RoleLevel.SUPER.name())
                .description("超级管理员")
                .build());
        
        return options;
    }
}
