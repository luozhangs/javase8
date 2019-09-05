package com.reflect;

import java.lang.annotation.*;

/**
 * Created by zhang on 2019/4/10.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Excel {
    String color() default "green";
    String value();
    int size() default 12;
    int length() default 20;
}
