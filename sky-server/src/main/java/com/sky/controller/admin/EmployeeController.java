package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.vo.EmployeeDetailVO;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
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
    public Result<String> changePassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        log.info("修改密码请求");
        employeeService.changePassword(passwordEditDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("查询员工信息")
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
    public Result<String> changeEmployee(@RequestBody EmployeeUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("id is required");
        }
        employeeService.changeEmployee(dto);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用,禁用员工账号")
    public Result<?> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("status must be 0 or 1");
        }
        if (id == null) {
            return Result.error("id is required");
        }
        employeeService.updateStatus(id, status);
        return Result.success();
    }
}
