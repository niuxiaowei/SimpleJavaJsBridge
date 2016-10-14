package com.simplejsjavabridge.lib.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解是用来标注java为js提供的接口，{@link #value()}的值是代表java与js之间约定好的接口名字。
 * <p>例子：</p>
 * <pre>
 *
 *      :@JavaInterface4JS("test")
 *      public void test(@Param("msg") String msg);
 *
 *      上面的例子，表明java为js提供一个名字为{@code test}的接口
 * </pre>
 * Created by niuxiaowei on 2015/10/27.
 * @see Param
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JavaInterface4JS {
    /**
     * 代表java与js之间约定好的接口名字
     * @return
     */
    String value();
}
