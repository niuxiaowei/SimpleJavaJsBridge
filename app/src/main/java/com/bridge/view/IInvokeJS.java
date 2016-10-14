package com.bridge.view;


import com.simplejsjavabridge.lib.IJavaCallback2JS;
import com.simplejsjavabridge.lib.annotation.InvokeJSInterface;
import com.simplejsjavabridge.lib.annotation.Param;
import com.simplejsjavabridge.lib.annotation.ParamCallback;

/**
 * Created by niuxiaowei on 16/8/28.
 */
public interface IInvokeJS {


    public static class City{
        @Param("cityName")
        public String cityName;

        @Param("cityProvince")
        public String cityProvince;

        public int cityId;


    }

    @InvokeJSInterface("exam")
    void exam(@Param("test") String testContent, @Param("id") int id,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJSInterface("exam1")
    void exam1(@Param(needConvert = true) City city, @ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJSInterface("exam2")
    void exam2(@Param(needConvert = true) City city, @Param("contry") String  contry,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJSInterface("exam3")
    void exam3(@Param(value = "city",needConvert = true) City city, @Param("contry") String  contry,@ParamCallback IJavaCallback2JS iJavaCallback2JS);

    @InvokeJSInterface("exam4")
    void exam4(@ParamCallback IJavaCallback2JS iJavaCallback2JS);
}
