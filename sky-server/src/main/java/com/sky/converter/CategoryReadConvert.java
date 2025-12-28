package com.sky.converter;

import com.sky.entity.Category;
import com.sky.readmodel.category.CategoryDetailRM;
import com.sky.readmodel.category.CategoryPageRM;
import com.sky.vo.category.CategoryDetailVO;
import com.sky.vo.category.CategoryPageVO;
import com.sky.vo.category.CategorySimpleVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 分类读转换器 - 负责分类的读操作转换
 * 
 * <p>包含：
 * <ul>
 *   <li>RM -> VO 的转换</li>
 *   <li>Entity -> SimpleVO 的转换（用于下拉选择）</li>
 * </ul>
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryReadConvert {

    /**
     * 列表页：PageRM -> PageVO
     */
    CategoryPageVO toPageVO(CategoryPageRM rm);

    /**
     * 批量转换：PageRM List -> PageVO List
     */
    List<CategoryPageVO> toPageVOList(List<CategoryPageRM> rmList);

    /**
     * 详情页：DetailRM -> DetailVO
     */
    CategoryDetailVO toDetailVO(CategoryDetailRM rm);

    /**
     * 下拉选择：Entity -> SimpleVO
     */
    CategorySimpleVO toSimpleVO(Category category);

    /**
     * 批量转换：Entity List -> SimpleVO List
     */
    List<CategorySimpleVO> toSimpleVOList(List<Category> categories);
}
