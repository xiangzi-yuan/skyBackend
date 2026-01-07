package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.vo.category.CategorySimpleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 分类管理
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "分类相关接口")
@Slf4j
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 根据类型查询分类列表, 不传参数代表全选
     * @param type 分类类型（1-菜品分类，2-套餐分类）
     * @return 分类列表
     */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<CategorySimpleVO>> list(@RequestParam(required = false) Integer type) {
        log.info("根据类型查询分类：type={}", type);
        List<CategorySimpleVO> list = categoryService.list(type);
        return Result.success(list);
    }
}
