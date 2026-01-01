package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.dish.DishDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Validated
public class DishController {
    @Autowired
    private DishService dishService;


    @GetMapping("/{id}")
    @ApiOperation("根据ID查询菜品详情")
    public Result<DishDetailVO> getById(@PathVariable Long id) {
        log.info("查询菜品详情：id={}", id);
        DishDetailVO dishDetailVO = dishService.getDetailById(id);
        return Result.success(dishDetailVO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishDetailVO>>  getByCategoryId(@RequestParam Long categoryId){
        log.info("根据分类id:{}查询菜品", categoryId);
        List<DishDetailVO> dishDetailVOList = dishService.listOnSaleByCategoryId(categoryId);
        return Result.success(dishDetailVOList);
    }

}
