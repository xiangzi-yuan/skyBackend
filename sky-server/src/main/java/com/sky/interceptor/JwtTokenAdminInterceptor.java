package com.sky.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.mapper.EmployeeMapper;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.role.RoleLevel;
import com.sky.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 非 Controller 方法（静态资源等）直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1) 从请求头取 token（header 名由配置 sky.jwt.adminTokenName 决定）
        String token = request.getHeader(jwtProperties.getAdminTokenName());
        if (token == null || token.isBlank()) {
            response.setStatus(401);
            return false;
        }

        try {
            // 2) 解析 JWT
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

            // 4) 强制改密：未改密 -> 403（白名单除外）
            Integer pwdChanged = employeeMapper.getPwdChangedById(empId);
            if (pwdChanged == null || pwdChanged == 0) {
                writeJson403(response, MessageConstant.PASSWORD_NEED_CHANGE);
                return false;
            }

            // 5) RBAC：三级权限
            if (!checkPermission(role, uri, method)) {
                writeJson403(response, MessageConstant.NO_PERMISSION);
                return false;
            }

            return true;

        } catch (Exception ex) {
            log.warn("jwt校验失败：{}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    /**
     * 白名单：只要已登录即可访问（并且允许未改密访问）
     */
    private boolean isLoginOnlyWhitelist(String uri, String method) {
        return ("/admin/employee/logout".equals(uri) && "POST".equalsIgnoreCase(method))
                || ("/admin/employee/editPassword".equals(uri) && "PUT".equalsIgnoreCase(method));
    }

    /**
     * 三级权限校验
     */
    private boolean checkPermission(RoleLevel role, String uri, String method) {

        // SUPER：全放行
        if (role == RoleLevel.SUPER) {
            return true;
        }

        // MANAGER：除员工管理外都放行（员工管理一律拒绝）
        if (role == RoleLevel.MANAGER) {
            // 员工管理接口全拒绝（login/logout/editPassword 已在白名单提前 return）
            return !uri.startsWith("/admin/employee");
        }

        // STAFF：只允许自用 + 低风险只读（你说的数据范围控制暂不做）
        if (role == RoleLevel.STAFF) {
            // 分类下拉
            if ("/admin/category/list".equals(uri) && "GET".equalsIgnoreCase(method)) return true;

            // 菜品/套餐只读
            if (uri.startsWith("/admin/dish") && "GET".equalsIgnoreCase(method)) return true;
            if (uri.startsWith("/admin/setmeal") && "GET".equalsIgnoreCase(method)) return true;

            // 订单只读
            if (uri.startsWith("/admin/order") && "GET".equalsIgnoreCase(method)) return true;

            // 其它一律拒绝
            return false;
        }

        // 兜底：未知角色默认拒绝
        return false;
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
