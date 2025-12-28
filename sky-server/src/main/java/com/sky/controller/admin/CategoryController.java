package com.sky.controller.admin;

import com.sky.dto.category.CategoryCreateDTO;
import com.sky.dto.category.CategoryPageQueryDTO;
import com.sky.dto.category.CategoryUpdateDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.vo.category.CategoryDetailVO;
import com.sky.vo.category.CategorySimpleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 分类管理
 */
@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param dto 新增分类DTO
     * @return 操作结果
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result<String> save(@Valid @RequestBody CategoryCreateDTO dto) {
        log.info("新增分类：{}", dto);
        categoryService.save(dto);
        return Result.success();
    }

    /**
     * 分类分页查询
     * @param dto 分页查询条件
     * @return 分页结果
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO dto) {
        log.info("分页查询：{}", dto);
        PageResult pageResult = categoryService.pageQuery(dto);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询分类详情（用于编辑回显）
     * @param id 分类ID
     * @return 分类详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询分类详情")
    public Result<CategoryDetailVO> getById(@PathVariable Long id) {
        log.info("查询分类详情：{}", id);
        CategoryDetailVO detailVO = categoryService.getById(id);
        return Result.success(detailVO);
    }

    /**
     * 删除分类
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping
    @ApiOperation("删除分类")
    public Result<String> deleteById(Long id) {
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改分类
     * @param dto 修改分类DTO
     * @return 操作结果
     */
    @PutMapping
    @ApiOperation("修改分类")
    public Result<String> update(@Valid @RequestBody CategoryUpdateDTO dto) {
        log.info("修改分类：{}", dto);
        categoryService.update(dto);
        return Result.success();
    }

    /**
     * 启用/禁用分类
     * @param status 状态值
     * @param id 分类ID
     * @return 操作结果
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result<String> startOrStop(@PathVariable("status") Integer status, Long id) {
        log.info("启用禁用分类：status={}, id={}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    /**
     * 根据类型查询分类列表（用于下拉选择）
     * @param type 分类类型
     * @return 分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<CategorySimpleVO>> list(Integer type) {
        log.info("根据类型查询分类：type={}", type);
        List<CategorySimpleVO> list = categoryService.list(type);
        return Result.success(list);
    }
}
