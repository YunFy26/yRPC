package org.example.spi;

import java.lang.annotation.*;

/*
 表示被修饰的注解应该被包含在JavaDoc中
 */
@Documented
/*
 注解的生命周期
 */
@Retention(RetentionPolicy.RUNTIME) // 保留到运行时，可通过反射读取
/*
 限制注解可以应用的目标元素类型
 */
@Target(ElementType.TYPE)
public @interface SPI {
}
