package com.sky.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_NEED_CHANGE = "首次登录请修改密码";
    public static final String ALREADY_EXISTS = "用户已存在";
    public static final String PASSWORD_ERROR = "密码错误 [CI/CD自动部署测试1]";
    public static final String OLD_PASSWORD_ERROR = "原密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String NOT_LOGIN = "未登录";
    public static final String USER_NOT_LOGIN = "用户未登录";
    public static final String CATEGORY_BE_RELATED_BY_SETMEAL = "当前分类关联了套餐,不能删除";
    public static final String CATEGORY_BE_RELATED_BY_DISH = "当前分类关联了菜品,不能删除";
    public static final String SHOPPING_CART_IS_NULL = "购物车数据为空，不能下单";
    public static final String ADDRESS_BOOK_IS_NULL = "用户地址为空，不能下单";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String SETMEAL_ENABLE_FAILED = "套餐内包含未启售菜品，无法启售";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";
    public static final String DISH_ON_SALE = "起售中的菜品不能删除";
    public static final String SETMEAL_ON_SALE = "起售中的套餐不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL = "当前菜品关联了套餐,不能删除";
    public static final String DISH_BE_RELATED_BY_SETMEAL_DETAIL = "当前菜品被以下套餐关联，不能删除：";
    public static final String ORDER_STATUS_ERROR = "订单状态错误";
    public static final String ORDER_NOT_FOUND = "订单不存在";
    public static final String NO_PERMISSION = "无权限";
    public static final String NO_PERMISSION_EMPLOYEE_READ = "当前角色无权查看员工信息";
    public static final String NO_PERMISSION_EMPLOYEE_WRITE = "当前角色无权修改员工信息";
    public static final String NO_PERMISSION_STAFF_READ = "普通员工无权查看该资源";
    public static final String NO_PERMISSION_STAFF_WRITE = "普通员工无权修改该资源";
    public static final String NO_PERMISSION_MANAGER_WRITE = "经理无权修改员工信息";
    public static final String NO_PERMISSION_UNKNOWN_ROLE = "未知角色，拒绝访问";

    // ===== 通用校验/参数错误 =====
    public static final String PARAM_VALID_FAILED = "参数校验失败";
    public static final String ID_REQUIRED = "id不能为空";
    public static final String STATUS_MUST_BE_0_OR_1 = "status must be 0 or 1";

    // ===== 业务不存在/状态不允许 =====
    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String DISH_NOT_FOUND_OR_UPDATE_FAILED = "菜品不存在或更新失败";
    public static final String SETMEAL_NOT_FOUND = "套餐不存在";
    public static final String SETMEAL_NOT_FOUND_OR_UPDATE_FAILED = "套餐不存在或更新失败";

    // ===== 密码相关 =====
    public static final String OLD_NEW_PASSWORD_REQUIRED = "oldPassword/newPassword is required";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "新密码不能与旧密码相同";
}
