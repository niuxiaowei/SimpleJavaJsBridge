package com.bridge.jsbridge.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by niuxiaowei on 2015/10/27.
 *
 * java会主动调用js提供的接口，该注解就是用来标注这些接口的,{@link #value()}js提供的接口的名字
 * <p>例子：</p>
 * <pre>
 *     :@InvokeJSInterface("exam")
 *     public void exam():
 *
 *     该例子表明java会调用js提供的{@code exam}这样的接口
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface InvokeJSInterface {
    String value();
}
