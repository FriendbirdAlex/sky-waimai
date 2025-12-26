package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut; // 必须是这个
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
// ... 其他业务包

/*切面类：实现对公共字段的自动填充功能*/
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 定义切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置通知：在执行插入操作前自动填充公共字段
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始自动填充公共字段...");
        //获取当前注解获取到的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();
        //获取当前被拦截方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if ((args == null) || args.length == 0) {
            return;
        }
        Object entity = args[0];
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentUserId = BaseContext.getCurrentId();
        //为四个公共字段赋值
        Method setCreateTimeMethod = null;
        if (operationType == OperationType.INSERT) {
            try {
                setCreateTimeMethod = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTimeMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUserMethod = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUserMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTimeMethod.invoke(entity, now);
                setCreateUserMethod.invoke(entity, currentUserId);
                setUpdateTimeMethod.invoke(entity, now);
                setUpdateUserMethod.invoke(entity, currentUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if(operationType == OperationType.UPDATE){
            try {
                Method setUpdateTimeMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUserMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTimeMethod.invoke(entity, now);
                setUpdateUserMethod.invoke(entity, currentUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
