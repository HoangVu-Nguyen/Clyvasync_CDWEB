package com.commoncore.security.annotation;


import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
/*
 * Giải thích: 'claims' là map chứa các dữ liệu trong JWT.
 * 'user_id' phải khớp chính xác với key bạn đã set trong TokenCustomizer.
 */
@AuthenticationPrincipal(expression = "claims['user_id']")
public @interface CurrentUserId {
}
