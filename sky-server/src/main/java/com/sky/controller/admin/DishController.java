package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;


    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> saveDish(@RequestBody DishDTO dto) {
        log.info("新增菜品：username={}", dto.getName());
        dishService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询菜品信息")
    public Result<PageResult> page(DishPageQueryDTO dto) {

        return Result.success(dishService.pageQuery(dto));
    }
    
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品详情")
    public Result<DishDetailVO> getById(@PathVariable Long id) {
        log.info("查询菜品详情：id={}", id);
        DishDetailVO dishDetailVO = dishService.getDetailById(id);
        return Result.success(dishDetailVO);
    }
}
