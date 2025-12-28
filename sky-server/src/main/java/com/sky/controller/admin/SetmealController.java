package com.sky.controller.admin;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.dto.SetmealUpdateDTO;
import com.sky.dto.SetmealCreateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealDetailVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result<String> saveSetmeal(@RequestBody SetmealCreateDTO dto) {
        log.info("新增套餐：name={}", dto.getName());
        setmealService.save(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("分页查询套餐信息")
    public Result<PageResult> page(SetmealPageQueryDTO dto) {

        return Result.success(setmealService.pageQuery(dto));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询套餐详情")
    public Result<SetmealDetailVO> getById(@PathVariable Long id) {
        log.info("查询套餐详情：id={}", id);
        SetmealDetailVO SetmealDetailVO = setmealService.getDetailById(id);
        return Result.success(SetmealDetailVO);
    }

    /**
     * 删除套餐
     * 业务规则:
     * 可以一次删除一个套餐
     * 也可以批量删除套餐
     * 起售中的套餐不能删除
     * 关联的菜品数据也需要删除掉
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐: {}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐起售和停售")
    public Result<String> updateSetmealSaleStatus(@PathVariable Integer status, @RequestParam Long id) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("status must be 0 or 1");
        }
        if (id == null) {
            return Result.error("id is required");
        }
        if (status == 0) log.info("停售套餐: {}", id);
        else log.info("起售套餐: {}", id);

        setmealService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐信息")
    public Result<String> changeSetmeal(@RequestBody SetmealUpdateDTO dto) {
        if (dto.getId() == null) {
            throw new IllegalArgumentException("id is required");
        }
        setmealService.changeSetmeal(dto);
        return Result.success();
    }

}
