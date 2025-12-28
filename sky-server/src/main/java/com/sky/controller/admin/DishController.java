package com.sky.controller.admin;

import com.sky.dto.DishCreateDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.DishUpdateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;


    @PostMapping
    @ApiOperation("新增菜品")
    public Result<String> saveDish(@RequestBody DishCreateDTO dto) {
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
        List<DishDetailVO> dishDetailVOList = dishService.getByCategoryId(categoryId);
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
    public Result<String> updateDishSaleStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("status must be 0 or 1");
        }
        if (id == null) {
            return Result.error("id is required");
        }
        if (status == 0) log.info("停售菜品: {}", id);
        else log.info("起售菜品: {}", id);

        dishService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改菜品信息")
    public Result<String> changeDish(@RequestBody DishUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("id is required");
        }
        dishService.changeDish(dto);
        return Result.success();
    }

}
