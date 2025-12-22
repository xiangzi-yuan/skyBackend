package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO.getUsername());

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        // 把id和role都放进claims
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        claims.put(JwtClaimsConstant.EMP_ROLE, employee.getRole()); // 1/5/9
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);



        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .needChangePassword(employee.getPwdChanged() == null || employee.getPwdChanged() == 0)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation("员工登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * @param employeeCreateDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result<String> saveEmployee(@RequestBody EmployeeCreateDTO employeeCreateDTO){
        log.info("新增员工：username={}", employeeCreateDTO.getUsername());
        employeeService.save(employeeCreateDTO);
        return Result.success();
    }

    /**
     * 修改密码
     * @param passwordEditDTO
     * @return
     */
    @PutMapping("/editPassword")
    @ApiOperation("修改密码")
    public Result<String> changePassword(@RequestBody PasswordEditDTO passwordEditDTO){
        Long empId = BaseContext.getCurrentId();
        log.info("修改密码：empId={}", empId);
        employeeService.changePassword(passwordEditDTO);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    @ApiOperation("查询员工信息")
    public Result<PageResult> page (EmployeePageQueryDTO dto){  // 从url里读取参数, 不用
        if(dto.getName()==null) log.info("查询员工");
        else log.info("查询员工{}",dto.getName());
        return Result.success(employeeService.pageQuery(dto));
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息：{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param dto
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result<String> changeEmployee(@RequestBody EmployeeUpdateDTO dto){
        if (dto.getId() == null) {
            throw new IllegalArgumentException("id is required");
        }
        log.info("修改员工信息：id={}, username={}", dto.getId(), dto.getUsername());
        employeeService.changeEmployee(dto);
        return Result.success();
    }

    /**
     * 启用,禁用员工账号
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用,禁用员工账号")
    public Result<?> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("status must be 0 or 1");
        }
        if (id == null) {
            return Result.error("id is required");
        }
        log.info("改变账号状态 id={}, status={}", id, status);
        employeeService.updateStatus(id, status);
        return Result.success();
    }

}
