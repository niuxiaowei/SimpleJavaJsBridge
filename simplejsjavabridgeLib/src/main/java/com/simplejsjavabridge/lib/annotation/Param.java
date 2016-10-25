package com.simplejsjavabridge.lib.annotation;

import org.json.JSONArray;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 该注解用来标注方法中的一个参数或者一个类的实例属性。
 * java与js之间传递参数的格式是json，因为对于json的封装和解析是一件繁琐的重复的体力劳动，
 * 因此通过注解来解决此问题。
 * <p>把参数值转化为json格式</p>
 * <p>例子1：</p>
 * <pre>
 *     :@InvokeJSInterface("test")
 *     public void test(@Param("msg") String content)；
 *
 *     test方法主要是让使用者来调用js暴露的test接口时，最终会把{@code @Param("msg") String content}转化为{msg:"content"}的json
 *
 * </pre>
 * <p>例子2:</p>
 * <pre>
 *     :@InvokeJSInterface("test")
 *     public void test(@Param String content)；
 *
 *     最终把参数转化为{"content"}的json
 * </pre>
 * <p/>
 * <p>例子3:参数是{@link org.json.JSONObject}不可以直接存放的，{@link #value()}设置了值</p>
 * <pre>
 *
 *     class User{
 *         :@Param("userId")
 *         String userId;
 *
 *         :@Param("name")
 *         String userName;
 *     }
 *
 *     :@InvokeJSInterface("test")
 *     public void test(@Param(value="userInfo") User userInfo)；
 *
 *     最终把参数转化为{userInfo:{userId:"userId", name:"userName"}}的json
 * </pre>
 * <p/>
 * <p>例子4:参数是{@link org.json.JSONObject}不可以直接存放的</p>
 * <pre>
 *
 *     class User{
 *         :@Param("userId")
 *         String userId;
 *
 *         :@Param("name")
 *         String userName;
 *     }
 *
 *     :@InvokeJSInterface("test")
 *     public void test(@Param User userInfo)；
 *
 *     最终把参数转化为{userId:"userId", name:"userName"}的json
 * </pre>
 * <p/>
 * <p>把json转化为参数值</p>
 * <p>例子1:简单类型</p>
 * <pre>
 *
 *     :@JavaInterface4JS("exam")
 *     public void test(@Param("msg") String content){
 *
 *     }
 *
 *     test方法是java提供给js的接口，js调用该接口时，假如传递的json是{msg:"你好java"},那会把该json中的"你好java"赋值给test方法的content参数
 * </pre>
 * <p>例子2:{@link org.json.JSONObject}不能直接存放的类型</p>
 * <pre>
 *
 *     class User{
 *         :@Param("userId")
 *         String userId;
 *
 *         :@Param("name")
 *         String userName;
 *     }
 *     :@JavaInterface4JS("exam")
 *     public void test(@Param("userInfo") User userInfo){
 *
 *     }
 *
 *     假如传递的json是{userInfo:{userId:"11", name:"nihao"}},那会把该json中的userInfo:{userId:"11", name:"nihao"}赋值给test方法的userInfo参数
 * </pre>
 */
@Target(value = {ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Param {
    /**
     * <pre>json中一般是以{key:value, key1:value1}的格式组织数据，</pre>
     * <p>{@link #value()}就代表key，key1这些值，@Param标注的参数或类的实例属性代表value或value1这些值；</p>
     * {@link #value()}的值可以不设置，这种情况主要基于该注解所标注的对象实例是{@link org.json.JSONObject}不可以直接进行存取的
     * 其他情况建议设置{@link #value()}的值
     *
     * @return
     */
    String value() default "";

}
