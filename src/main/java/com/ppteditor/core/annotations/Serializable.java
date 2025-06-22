package com.ppteditor.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 序列化注解
 * 标记需要在项目保存时序列化的字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Serializable {
    /**
     * 序列化的键名，默认使用字段名
     */
    String value() default "";
    
    /**
     * 是否必需，如果为true且值为null则抛出异常
     */
    boolean required() default false;
} 