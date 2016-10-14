package com.simplejsjavabridge.lib.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 不管是java主动调用js，还是js主动调用java，都得需要提供一个回调函数，来获取返回结果，
 * 因此该注解用来标注提供给对方的回调方法.
 * <p>例子1:java主动调用js，提供给js的回调方法</p>
 * <pre>
 *
 *     :@InvokeJSInterface("test")
 *     public void invokeJSTest(@ParamCallback Object callback);
 *
 *     使用：invokeJSTest(new Object(){
 *         :@JavaCallback4JS
 *         public void callback(){
 *
 *         }
 *     });
 * </pre>
 *
 * <p>例子2:js主动调用java，提供给java的回调方法</p>
 * <pre>
 *     //声明一个回调接口
 *     interface IExamCallback{
 *         void examCallback();
 *     }
 *
 *     :@JavaInterface4JS("exam")
 *     public void examInterface4JS(@ParamCallback IExamCallback iExamCallback){
 *         iExamCallback.examCallback();
 *     }
 * </pre>
 *
 * @see JavaCallback4JS,JavaInterface4JS,InvokeJSInterface
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ParamCallback {
}
