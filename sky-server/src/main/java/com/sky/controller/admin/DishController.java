package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.dish.DishCreateDTO;
import com.sky.dto.dish.DishPageQueryDTO;
import com.sky.dto.dish.DishUpdateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.dish.DishDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Slf4j
@Validated
public class DishController {
    @Autowired
    private DishService dishService;


    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> saveDish(@Valid @RequestBody DishCreateDTO dto) {
        log.info("新增菜品：name={}", dto.getName());
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

    @GetMapping("/list")
    @ApiOperation("根据分类查询菜品")
    public Result<List<DishDetailVO>>  getByCategoryId(@RequestParam Long categoryId){
        log.info("根据分类id:{}查询菜品", categoryId);
        List<DishDetailVO> dishDetailVOList = dishService.listAllByCategoryId(categoryId);
        return Result.success(dishDetailVOList);
    }
    /**
     * 删除菜品
     * 业务规则:
     * 可以一次删除一个菜品
     * 也可以批量删除菜品起售中的菜品
     * 不能删除被套餐关联的菜品不能删除删除菜品压
     * 关联的口味数据也需要删除掉
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("批量删除菜品: {}", ids);
        dishService.delete(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("起售和停售")
    public Result<String> updateDishSaleStatus(
            @PathVariable @Min(value = 0, message = MessageConstant.STATUS_MUST_BE_0_OR_1) @Max(value = 1, message = MessageConstant.STATUS_MUST_BE_0_OR_1) Integer status,
            @RequestParam @NotNull(message = MessageConstant.ID_REQUIRED) Long id
    ) {
        if (status == 0) log.info("停售菜品: {}", id);
        else log.info("起售菜品: {}", id);

        dishService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result<String> changeDish(@Valid @RequestBody DishUpdateDTO dto) {
        dishService.changeDish(dto);
        return Result.success();
    }

}
