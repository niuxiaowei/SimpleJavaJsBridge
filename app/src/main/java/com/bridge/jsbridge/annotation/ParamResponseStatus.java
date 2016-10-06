package com.bridge.jsbridge.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 不管是java还是js主动调用对方的数据我们称作request，返回给对方的数据我们称作response。
 * response数据又包含responsestatus（响应状态数据）和其他数据组成。
 * <p>因此该注解是用来对responseStatus进行标注的，responseStatus格式({status:1, msg:"ok"}。{@link #value()}就是status或msg这些key值</p>
 * <p>例子：</p>
 * <pre>
 *
 *     public void receiveResponse(@ParamResponseStatus("status") int status, @ParamResponseStatus("msg") String msg){
 *
 *     }
 *
 *
 * </pre>
 *
 * Created by niuxiaowei on 2015/10/27.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ParamResponseStatus {
    /**
     * responseStatus格式({status:1, msg:"ok"}。{@link #value()}就是status或msg这些key值
     * @return
     */
    String value();
}
