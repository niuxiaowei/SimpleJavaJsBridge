package com.simplejsjavabridge.lib;

import android.text.TextUtils;


import com.simplejsjavabridge.lib.annotation.Param;
import com.simplejsjavabridge.lib.annotation.ParamCallback;
import com.simplejsjavabridge.lib.annotation.ParamResponseStatus;
import com.simplejsjavabridge.lib.exception.SimpleJSBridgeException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 该类会把{@link Method}的用{@link Param},{@link ParamCallback},{@link ParamResponseStatus}这几个注解标注的param解析出来，
 * {@link Param}解析为{@link ParamItem},{@link ParamCallback}解析为{@link ParamCallbackItem},{@link ParamResponseStatus}
 * 解析为{@link ParamResponseStatusItem}。
 * <p>同时该类还有把一个json转化为参数值的功能，和把参数值转化为json的功能</p>
 *
 * Created by niuxiaowei on 16/7/14.
 */
public class Params {

    /**
     * 解析出来的所有注解item
     */
    private BaseParamItem[] mParamItems;
    private static SimpleJavaJsBridge sSimpleJavaJsBridge;

    Params() {
    }

    /**
     * 初始化方法
     * @param simpleJavaJsBridge
     */
    public static void init(SimpleJavaJsBridge simpleJavaJsBridge) {
        sSimpleJavaJsBridge = simpleJavaJsBridge;
    }

    /**
     * 把json转化为参数值
     * @param requestResponseBuilder 包含了一系列的json数据，json数据是request或者response
     * @return
     */
    public Object[] convertJson2ParamValues(RequestResponseBuilder requestResponseBuilder) {
        if (requestResponseBuilder == null || mParamItems == null) {
            return null;
        }
        Object[] result = new Object[mParamItems.length];
        BaseParamItem paramItem = null;
        for (int i = 0; i < mParamItems.length; i++) {
            paramItem = mParamItems[i];
            if (paramItem != null) {

                result[i] = paramItem.convertJson2ParamValue(requestResponseBuilder);
            }
        }
        return result;

    }

    /**
     * 把参数值转化为json
     * @param requestResponseBuilder
     * @param paramValues 参数值
     */
    public void convertParamValues2Json(RequestResponseBuilder requestResponseBuilder, Object[] paramValues) {
        if (requestResponseBuilder == null || paramValues == null) {
            return;
        }
        BaseParamItem paramItem = null;
        for (int i = 0; i < mParamItems.length; i++) {
            paramItem = mParamItems[i];
            if (paramItem != null) {
                paramItem.convertParamValue2Json(requestResponseBuilder, paramValues[i]);
            }
        }
    }


    /**
     * 基础类，定义了一些基础的数据
     */
    private static abstract class BaseParamItem {
        /**
         * 参数所对应的类型{@link Class}
         */
        protected Class paramType;
        /**
         * 因为参数是由{@link Param},{@link ParamCallback},{@link ParamResponseStatus}其中的一个注解标注的，
         * 注解标注的参数，会以{key:value}的格式存入json中，key值就是注解的value()值，因此{@link #paramKey}来代表key值
         */
        protected String paramKey;

        public BaseParamItem(Class paramType, String paramKey) {
            this.paramType = paramType;
            this.paramKey = paramKey;
        }

        /**
         * json的格式{key:value}，该方法会从json中把value给解析出来，作为参数值
         * @param requestResponseBuilder
         * @return
         */
        public abstract Object convertJson2ParamValue(RequestResponseBuilder requestResponseBuilder);

        /**
         * 该方法会把参数值以{key:value}的格式存入json中
         * @param requestResponseBuilder
         * @param obj
         */
        public abstract void convertParamValue2Json(RequestResponseBuilder requestResponseBuilder, Object obj);
    }

    /**
     * 对应{@link Param}注解标注的参数
     */
    private static class ParamItem extends BaseParamItem {

        public boolean needConvert;

        public ParamItem(String paramKey, Class paramClass, boolean needConvert) {
            super(paramClass, paramKey);
            this.needConvert = needConvert;
        }

        @Override
        public Object convertJson2ParamValue(RequestResponseBuilder requestResponseBuilder) {
            if (requestResponseBuilder == null || requestResponseBuilder.getValues() == null) {
                return null;
            }
            JSONObject jsonObject = requestResponseBuilder.getValues();
            if (jsonObject != null) {
                if (needConvert) {
                    try {
                        JSONObject value = !TextUtils.isEmpty(paramKey) ? (JSONObject) jsonObject.opt(paramKey) : jsonObject;
                        if(value == null){
                            return null;
                        }
                        Object instance = paramType.newInstance();
                        Field[] fields = paramType.getDeclaredFields();
                        for (Field field : fields
                                ) {
                            Param p = field.getAnnotation(Param.class);
                            if (p != null) {
                                /*可以访问不可以访问的变量*/
                                field.setAccessible(true);
                                field.set(instance, value.opt(p.value()));
                            }
                        }
                        return instance;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    return jsonObject.opt(paramKey);
                }
            }
            return null;
        }

        @Override
        public void convertParamValue2Json(RequestResponseBuilder requestResponseBuilder, Object obj) {

            if (requestResponseBuilder == null || obj == null) {
                return;
            }
            /*需要进行转换*/
            if (needConvert) {
                JSONObject objectParamJson = null;
                if (!TextUtils.isEmpty(paramKey)) {
                    objectParamJson = new JSONObject();
                }

                Class cl = obj.getClass();
                Field[] fields = cl.getDeclaredFields();
                for (Field field : fields
                        ) {
                    Param p = field.getAnnotation(Param.class);
                    if (p != null) {
                                                                        /*可以访问不可以访问的变量*/
                        field.setAccessible(true);
                        Object inst = null;
                        try {
                            inst = field.get(obj);
                            if (inst != null) {
                                if (objectParamJson != null) {

                                    objectParamJson.put(p.value(), inst);
                                } else {
                                    requestResponseBuilder.putValue(p.value(), inst);
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (objectParamJson != null) {
                    requestResponseBuilder.putValue(paramKey, objectParamJson);
                }
            } else {
                requestResponseBuilder.putValue(paramKey, obj);

            }
        }
    }


    /**
     * 对应{@link ParamResponseStatus}注解标注的参数
     */
    private static class ParamResponseStatusItem extends BaseParamItem {

        public ParamResponseStatusItem(Class paramClass, String paramKey) {
            super(paramClass, paramKey);
        }

        @Override
        public Object convertJson2ParamValue(RequestResponseBuilder requestResponseBuilder) {
            if (requestResponseBuilder == null || requestResponseBuilder.getResponseStatus() == null ) {
                return null;
            }
            return requestResponseBuilder.getResponseStatus().opt(paramKey);
        }

        @Override
        public void convertParamValue2Json(RequestResponseBuilder requestResponseBuilder, Object obj) {

            if (requestResponseBuilder == null || obj == null) {
                return;
            }
            requestResponseBuilder.putResponseStatus(paramKey, obj);
        }
    }

    /**
     * 对应{@link ParamCallback}注解标注的参数
     */
    private static class ParamCallbackItem extends BaseParamItem {


        public ParamCallbackItem(Class callbackClass, String paramKey) {
            super(callbackClass, paramKey);
        }

        @Override
        public Object convertJson2ParamValue( RequestResponseBuilder requestResponseBuilder) {
            if (requestResponseBuilder == null || requestResponseBuilder.getCallbackId() == null) {
                return null;
            }
            final String resId = requestResponseBuilder.getCallbackId();
            return Proxy.newProxyInstance(paramType.getClassLoader(), new Class<?>[]{paramType},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                            RequestResponseBuilder response = new RequestResponseBuilder(false);
                            response.setResponseId(resId);
                            Params params = Params.createParams(method);
                            params.convertParamValues2Json(response, args);
                            if (sSimpleJavaJsBridge != null) {

                                sSimpleJavaJsBridge.sendData2JS(response);
                            } else {
                                throw new SimpleJSBridgeException(SimpleJavaJsBridge.class.getName() + "必须得进行初始化");
                            }
                            return new Object();
                        }
                    }

            );
        }

        @Override
        public void convertParamValue2Json(RequestResponseBuilder requestResponseBuilder, Object obj) {

            if (requestResponseBuilder == null || obj == null || !(obj instanceof IJavaCallback2JS)) {
                return;
            }
            requestResponseBuilder.setRequestCallback((IJavaCallback2JS)obj);

        }
    }


    /**
     * 从{@link Method}中解析它所包含的参数
     *
     * @param method
     * @return
     */
    public static Params createParams(Method method) {
        if (method != null) {
            Annotation[][] annotations = method.getParameterAnnotations();
            Class[] parameters = method.getParameterTypes();
            if (annotations != null) {
                Params params = new Params();
                params.mParamItems = new BaseParamItem[annotations.length];
                BaseParamItem paramItem = null;
                for (int i = 0; i < annotations.length; i++) {
                    Annotation annotation = null;
                    if(annotations[i].length == 0){
                        throw new IllegalArgumentException("方法的所有参数必须都得用"+Param.class.getSimpleName()+","+ParamCallback.class.getSimpleName()+","+ParamResponseStatus.class.getSimpleName()+" 中的任意一个注解进行标注");

                    }
                    for (int j = 0; j < annotations[i].length; j++) {
                        annotation = annotations[i][j];
                        if (annotation != null && annotation instanceof Param) {
                            Param paramKey = (Param) annotation;
                            paramItem = new ParamItem(paramKey.value(), parameters[i], paramKey.needConvert());
                            params.mParamItems[i] = paramItem;
                        } else if (annotation instanceof ParamCallback) {
                            paramItem = new ParamCallbackItem(parameters[i], null);
                            params.mParamItems[i] = paramItem;
                        } else if (annotation instanceof ParamResponseStatus) {
                            ParamResponseStatus paramResponseStatus = (ParamResponseStatus)annotation;
                            paramItem = new ParamResponseStatusItem(parameters[i], paramResponseStatus.value());
                            params.mParamItems[i] = paramItem;
                        }
                    }
                }

                return params;
            }

        }
        return null;
    }


}
