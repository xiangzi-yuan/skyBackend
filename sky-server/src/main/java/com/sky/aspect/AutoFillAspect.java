package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Aspect
@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AutoFillAspect {

    @Pointcut("@annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Around("autoFillPointCut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        AutoFill autoFill = method.getAnnotation(AutoFill.class);
        if (autoFill == null) {
            return pjp.proceed();
        }

        OperationType type = autoFill.value();
        LocalDateTime now = LocalDateTime.now();

        Long userId = null;
        try {
            userId = BaseContext.getCurrentId();
        } catch (Exception ignore) {
            // 允许某些场景没有上下文（比如任务、测试）
        }

        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return pjp.proceed();
        }

        // 1) 填充实体参数（insert/update 常见）
        for (Object arg : args) {
            if (arg == null) continue;

            if (arg instanceof Map) {
                fillMap((Map<?, ?>) arg, type, now, userId);
                continue;
            }

            if (isSimpleValueType(arg.getClass())) continue;

            fillBean(arg, type, now, userId);
        }

        // 2) 如有 @Param(updateTime/updateUser/...) 多参方式，也能填
        Object[] newArgs = args.clone();
        boolean changed = fillParamArgsIfAny(method, newArgs, type, now, userId);

        if (changed) {
            return pjp.proceed(newArgs);
        }
        return pjp.proceed();
    }

    private void fillBean(Object target, OperationType type, LocalDateTime now, Long userId) {
        switch (type) {
            case INSERT:
                invokeIfExists(target, "setCreateTime", LocalDateTime.class, now);
                invokeIfExists(target, "setUpdateTime", LocalDateTime.class, now);
                if (userId != null) {
                    invokeIfExists(target, "setCreateUser", Long.class, userId);
                    invokeIfExists(target, "setUpdateUser", Long.class, userId);
                }
                break;
            case UPDATE:
                invokeIfExists(target, "setUpdateTime", LocalDateTime.class, now);
                if (userId != null) {
                    invokeIfExists(target, "setUpdateUser", Long.class, userId);
                }
                break;
            default:
                // no-op
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void fillMap(Map map, OperationType type, LocalDateTime now, Long userId) {
        switch (type) {
            case INSERT:
                map.putIfAbsent("createTime", now);
                map.putIfAbsent("updateTime", now);
                if (userId != null) {
                    map.putIfAbsent("createUser", userId);
                    map.putIfAbsent("updateUser", userId);
                }
                break;
            case UPDATE:
                map.putIfAbsent("updateTime", now);
                if (userId != null) {
                    map.putIfAbsent("updateUser", userId);
                }
                break;
            default:
        }
    }

    private boolean fillParamArgsIfAny(Method method, Object[] args, OperationType type, LocalDateTime now, Long userId) {
        boolean changed = false;
        Parameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            Param p = params[i].getAnnotation(Param.class);
            if (p == null) continue;

            String name = p.value();

            if (type == OperationType.INSERT) {
                if ("createTime".equals(name) && args[i] == null) { args[i] = now; changed = true; }
                if ("updateTime".equals(name) && args[i] == null) { args[i] = now; changed = true; }
                if ("createUser".equals(name) && args[i] == null && userId != null) { args[i] = userId; changed = true; }
                if ("updateUser".equals(name) && args[i] == null && userId != null) { args[i] = userId; changed = true; }
            } else if (type == OperationType.UPDATE) {
                if ("updateTime".equals(name) && args[i] == null) { args[i] = now; changed = true; }
                if ("updateUser".equals(name) && args[i] == null && userId != null) { args[i] = userId; changed = true; }
            }
        }
        return changed;
    }

    private void invokeIfExists(Object target, String methodName, Class<?> paramType, Object value) {
        try {
            Method m = target.getClass().getMethod(methodName, paramType);
            m.invoke(target, value);
        } catch (NoSuchMethodException ignore) {
            // 没有这个 setter 就跳过（兼容不同实体）
        } catch (Exception e) {
            log.warn("AutoFill invoke {} on {} failed: {}", methodName, target.getClass().getName(), e.getMessage());
        }
    }

    private boolean isSimpleValueType(Class<?> clazz) {
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || CharSequence.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || LocalDateTime.class == clazz
                || Date.class.isAssignableFrom(clazz);
    }
}
