package com.campus.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import com.campus.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GlobalException.class)
    public Result<?> handleGlobalException(GlobalException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        return Result.error(401, "请先登录");
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRoleException(NotRoleException e) {
        return Result.error(403, "无权限执行此操作");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return Result.error(400, msg);
    }

    /**
     * 静态资源/未匹配路径不存在时返回 404，而非当作 500 系统异常。
     * 避免缺失图片等场景刷 ERROR 日志。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error(404, "资源不存在"));
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统内部异常", e);
        return Result.error(500, "系统内部异常，请稍后重试");
    }
}
