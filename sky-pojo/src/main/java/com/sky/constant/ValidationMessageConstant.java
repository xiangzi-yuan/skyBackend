package com.sky.constant;

/**
 * 参数校验提示常量（用于 DTO/VO 上的 javax.validation 注解 message 属性）。
 *
 * <p>注意：该模块不依赖 sky-common，因此这里单独维护校验相关文案常量。</p>
 */
public class ValidationMessageConstant {

    private ValidationMessageConstant() {
    }

    public static final String ID_REQUIRED = "id不能为空";

    public static final String CATEGORY_ID_REQUIRED = "分类ID不能为空";
    public static final String CATEGORY_TYPE_REQUIRED = "分类类型不能为空";
    public static final String CATEGORY_NAME_REQUIRED = "分类名称不能为空";
    public static final String SORT_REQUIRED = "排序不能为空";

    public static final String DISH_NAME_REQUIRED = "菜品名称不能为空";
    public static final String DISH_CATEGORY_REQUIRED = "菜品分类不能为空";
    public static final String DISH_PRICE_REQUIRED = "菜品价格不能为空";
    public static final String DISH_PRICE_MIN = "菜品价格必须大于0";
    public static final String IMAGE_REQUIRED = "图片不能为空";

    public static final String SETMEAL_CATEGORY_REQUIRED = "套餐分类不能为空";
    public static final String SETMEAL_NAME_REQUIRED = "套餐名称不能为空";
    public static final String SETMEAL_PRICE_REQUIRED = "套餐价格不能为空";
    public static final String SETMEAL_PRICE_MIN = "套餐价格必须大于0";

    public static final String USERNAME_REQUIRED = "用户名不能为空";
    public static final String PASSWORD_REQUIRED = "密码不能为空";
    public static final String OLD_PASSWORD_REQUIRED = "旧密码不能为空";
    public static final String NEW_PASSWORD_REQUIRED = "新密码不能为空";

    public static final String SEX_INVALID = "sex 只能是 0(女) 或 1(男)";
}

