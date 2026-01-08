package com.sky.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.readmodel.employee.EmployeeAuthInfo;
import com.sky.result.Result;
import com.sky.role.RoleLevel;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final EmployeeMapper employeeMapper;
    private final ObjectMapper objectMapper;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 非Controller方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1) 从请求头取token（header名由配置 sky.jwt.adminTokenName 决定）
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (token == null || token.isBlank()) {
            writeJson401(response, MessageConstant.NOT_LOGIN);
            return false;
        }

        try {
            // 2) 解析JWT
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);

            Long empId = Long.valueOf(String.valueOf(claims.get(JwtClaimsConstant.EMP_ID)));
            RoleLevel role = RoleLevel.from(claims.get(JwtClaimsConstant.EMP_ROLE));

            // 写入上下文
            BaseContext.setCurrentId(empId);
            BaseContext.setCurrentRole(role);

            String uri = request.getRequestURI();
            String method = request.getMethod();

            log.info("jwt校验通过：empId={}, role={}, uri={}, method={}", empId, role, uri, method);

            // 3) 已登录白名单（未改密也允许）：登出 / 改密
            //    login 不走这里（应在 WebMvcConfiguration.excludePathPatterns 放行）
            if (isLoginOnlyWhitelist(uri, method)) {
                return true;
            }

            // 4) 基于数据库最新状态做鉴权信息校验：禁用 / 是否改密
            //    目的：解决"禁用账号仍能带旧token访问"的问题
            EmployeeAuthInfo auth = employeeMapper.getAuthInfoById(empId);
            if (auth == null) {
                writeJson401(response, MessageConstant.ACCOUNT_NOT_FOUND);
                return false;
            }

            // 4.1) 禁用账号：直接拒绝
            if (auth.getStatus() != null && auth.getStatus() == StatusConstant.DISABLE) {
                writeJson403(response, MessageConstant.ACCOUNT_LOCKED);
                return false;
            }

            // 4.2) 强制改密：未改密 -> 403（白名单除外）
            if (auth.getPwdChanged() == null || auth.getPwdChanged() == 0) {
                writeJson403(response, MessageConstant.PASSWORD_NEED_CHANGE);
                return false;
            }

            // 5) RBAC：三级权限
            String permissionError = checkPermission(role, uri, method);
            if (permissionError != null) {
                writeJson403(response, permissionError);
                return false;
            }

            return true;

        } catch (Exception ex) {
            log.warn("jwt校验失败：{}", ex.getMessage());
            writeJson401(response, MessageConstant.NOT_LOGIN);
            return false;
        }
    }

    /**
     * 白名单：只需已登录即可访问（并且允许未改密访问）
     */
    private boolean isLoginOnlyWhitelist(String uri, String method) {
        return ("/admin/employee/logout".equals(uri) && "POST".equalsIgnoreCase(method))
                || ("/admin/employee/editPassword".equals(uri) && "PUT".equalsIgnoreCase(method));
    }

    /**
     * 三级权限校验
     * @return null 表示有权限；非null 返回具体错误信息
     */
    private String checkPermission(RoleLevel role, String uri, String method) {

        // SUPER：全放行
        if (role == RoleLevel.SUPER) {
            return null;
        }

        // MANAGER：可读写除员工外的所有接口
        if (role == RoleLevel.MANAGER) {
            // 员工管理模块：只允许GET（分页、按id查询），禁止写操作
            if (uri.startsWith("/admin/employee")) {
                if ("GET".equalsIgnoreCase(method)) {
                    return null;
                }
                return MessageConstant.NO_PERMISSION_MANAGER_WRITE;
            }
            // 其它模块全放行（订单、分类、菜品、套餐等可读可写）
            return null;
        }

        // STAFF：可读订单/分类/菜品/套餐，但不能修改；完全禁止员工管理
        if (role == RoleLevel.STAFF) {
            // 员工管理模块：完全禁止
            if (uri.startsWith("/admin/employee")) {
                return "GET".equalsIgnoreCase(method) 
                    ? MessageConstant.NO_PERMISSION_STAFF_READ 
                    : MessageConstant.NO_PERMISSION_STAFF_WRITE;
            }

            // 分类管理：只读
            if (uri.startsWith("/admin/category")) {
                return "GET".equalsIgnoreCase(method) 
                    ? null 
                    : MessageConstant.NO_PERMISSION_STAFF_WRITE;
            }

            // 菜品管理：只读
            if (uri.startsWith("/admin/dish")) {
                return "GET".equalsIgnoreCase(method) 
                    ? null 
                    : MessageConstant.NO_PERMISSION_STAFF_WRITE;
            }

            // 套餐管理：只读
            if (uri.startsWith("/admin/setmeal")) {
                return "GET".equalsIgnoreCase(method) 
                    ? null 
                    : MessageConstant.NO_PERMISSION_STAFF_WRITE;
            }

            // 订单管理：员工可读；可做“流转类写操作”（接单/拒单/派送/完成）；取消仍禁止
            if (uri.startsWith("/admin/order")) {

                // 读：放行（订单搜索、详情、统计等）
                if ("GET".equalsIgnoreCase(method)) {
                    return null;
                }

                // 写：只允许 PUT 且是以下动作
                if ("PUT".equalsIgnoreCase(method)) {
                    boolean allow =
                            uri.startsWith("/admin/order/confirm")   // 接单
                                    || uri.startsWith("/admin/order/rejection") // 拒单
                                    || uri.startsWith("/admin/order/delivery")  // 派送
                                    || uri.startsWith("/admin/order/complete"); // 完成

                    return allow ? null : MessageConstant.NO_PERMISSION_STAFF_WRITE;
                }

                // 其它 method 一律不允许
                return MessageConstant.NO_PERMISSION_STAFF_WRITE;
            }


            // 其它模块一律拒绝
            return "GET".equalsIgnoreCase(method) 
                ? MessageConstant.NO_PERMISSION_STAFF_READ 
                : MessageConstant.NO_PERMISSION_STAFF_WRITE;
        }

        // 兜底：未知角色默认拒绝
        return MessageConstant.NO_PERMISSION_UNKNOWN_ROLE;
    }

    private void writeJson401(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> r = Result.error(msg);
        response.getWriter().write(objectMapper.writeValueAsString(r));
        response.getWriter().flush();
    }

    private void writeJson403(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        Result<Object> r = Result.error(msg);
        response.getWriter().write(objectMapper.writeValueAsString(r));
        response.getWriter().flush();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
        BaseContext.removeCurrentRole();
    }
}