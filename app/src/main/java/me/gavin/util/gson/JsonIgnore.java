package me.gavin.util.gson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gson 序列化过滤字段注解
 *
 * @author gavin.xiong 2018/3/7
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JsonIgnore {

    boolean serialize() default false;

    boolean deserialize() default false;
}
