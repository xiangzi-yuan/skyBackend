package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.PasswordEditDTO;
import com.sky.dto.employee.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.vo.employee.EmployeeDetailVO;
import com.sky.vo.employee.EmployeeLoginVO;
import com.sky.vo.employee.EmployeeRoleVO;
import com.sky.vo.employee.RoleLevelOptionVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Validated
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@Valid @RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO.getUsername());
        return Result.success(employeeService.login(employeeLoginDTO));
    }

    @ApiOperation("员工登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @PostMapping
    @ApiOperation("新增员工")
    public Result<String> saveEmployee(@Validated @RequestBody EmployeeCreateDTO employeeCreateDTO) {
        log.info("新增员工：username={}", employeeCreateDTO.getUsername());
        employeeService.save(employeeCreateDTO);
        return Result.success();
    }

    @PutMapping("/editPassword")
    @ApiOperation("修改密码")
    public Result<String> changePassword(@Valid @RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("修改密码请求");
        employeeService.changePassword(passwordEditDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询员工信息")
    public Result<PageResult> page(EmployeePageQueryDTO dto) {
        return Result.success(employeeService.pageQuery(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<EmployeeDetailVO> getById(@PathVariable Long id) {
        return Result.success(employeeService.getById(id));
    }

    @PutMapping
    @ApiOperation("修改员工信息")
    public Result<String> changeEmployee(@Valid @RequestBody EmployeeUpdateDTO dto) {
        employeeService.changeEmployee(dto);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用,禁用员工账号")
    public Result<?> updateStatus(
            @PathVariable @Min(value = 0, message = MessageConstant.STATUS_MUST_BE_0_OR_1) @Max(value = 1, message = MessageConstant.STATUS_MUST_BE_0_OR_1) Integer status,
            @RequestParam @NotNull(message = MessageConstant.ID_REQUIRED) Long id
    ) {
        employeeService.updateStatus(id, status);
        return Result.success();
    }

    @PostMapping("/upgradeRole")
    @ApiOperation("提升员工权限（仅SUPER可用）")
    public Result<String> upgradeRole(@Valid @RequestBody EmployeeRoleUpgradeDTO dto) {
        log.info("提升员工权限：employeeId={}, newRole={}", dto.getEmployeeId(), dto.getNewRole());
        employeeService.upgradeRole(dto);
        return Result.success();
    }

    @GetMapping("/role/{employeeId}")
    @ApiOperation("获取员工权限信息")
    public Result<EmployeeRoleVO> getRoleInfo(@PathVariable Long employeeId) {
        return Result.success(employeeService.getRoleInfo(employeeId));
    }

    @GetMapping("/roleLevels")
    @ApiOperation("获取所有权限等级选项")
    public Result<List<RoleLevelOptionVO>> getRoleLevelOptions() {
        return Result.success(employeeService.getRoleLevelOptions());
    }
}
