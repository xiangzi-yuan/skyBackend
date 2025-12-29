package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.dto.setmeal.SetmealPageQueryDTO;
import com.sky.dto.setmeal.SetmealUpdateDTO;
import com.sky.dto.setmeal.SetmealCreateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.setmeal.SetmealDetailVO;
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

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Validated
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    public Result<String> saveSetmeal(@Valid @RequestBody SetmealCreateDTO dto) {
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
    public Result<String> updateSetmealSaleStatus(
            @PathVariable @Min(value = 0, message = MessageConstant.STATUS_MUST_BE_0_OR_1) @Max(value = 1, message = MessageConstant.STATUS_MUST_BE_0_OR_1) Integer status,
            @RequestParam @NotNull(message = MessageConstant.ID_REQUIRED) Long id
    ) {
        if (status == 0) log.info("停售套餐: {}", id);
        else log.info("起售套餐: {}", id);

        setmealService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐信息")
    public Result<String> changeSetmeal(@Valid @RequestBody SetmealUpdateDTO dto) {
        setmealService.changeSetmeal(dto);
        return Result.success();
    }

}
