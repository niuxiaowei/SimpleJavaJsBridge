package com.bridge.jsbridge.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * java在主动调用js的时候，同时会给js传递一个回调，该回调的作用就是为了监听js的返回结果，
 * 该注解的主要作用是为了标记java为js提供的回调方法.
 * <p>例子：</p>
 * <pre>
 *
 *  new Object{
 *      ;@JavaCallback4JS
 *      public void callback4JS()
 *  }
 *
 *  上面的例子表明{@code callback4JS}方法是提供给js的回调方法
 *
 * </pre>
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface JavaCallback4JS {

}
