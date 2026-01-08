package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.dish.DishItemVO;
import com.sky.vo.setmeal.SetmealDetailVO;
import com.sky.vo.setmeal.SetmealListVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.RequiredArgsConstructor;

@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Validated
@RequiredArgsConstructor
public class SetmealController {
    private final SetmealService setmealService;


    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐详情")
    public Result<SetmealDetailVO> getById(@PathVariable Long id) {
        log.info("查询套餐详情：id={}", id);
        SetmealDetailVO SetmealDetailVO = setmealService.getDetailById(id);
        return Result.success(SetmealDetailVO);
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<SetmealListVO>> list(@RequestParam Long categoryId) {
        log.info("根据分类id查询套餐：categoryId={}", categoryId);
        return Result.success(setmealService.listOnSaleByCategoryId(categoryId));
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> dish(@PathVariable Long id) {
        log.info("根据套餐id查询包含的菜品：id={}", id);
        return Result.success(setmealService.getDishItemsBySetmealId(id));
    }


}
