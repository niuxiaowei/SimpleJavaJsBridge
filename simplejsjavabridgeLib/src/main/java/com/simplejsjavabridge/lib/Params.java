package com.simplejsjavabridge.lib;

import android.text.TextUtils;


import com.simplejsjavabridge.lib.annotation.Param;
import com.simplejsjavabridge.lib.annotation.ParamCallback;
import com.simplejsjavabridge.lib.annotation.ParamResponseStatus;
import com.simplejsjavabridge.lib.exception.SimpleJSBridgeException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Iterator;

/**
 * 该类会把{@link Method}的用{@link Param},{@link ParamCallback},{@link ParamResponseStatus}这几个注解标注的param解析出来，
 * {@link Param}解析为{@link ParamItem},{@link ParamCallback}解析为{@link ParamCallbackItem},{@link ParamResponseStatus}
 * 解析为{@link ParamResponseStatusItem}。
 * <p>同时该类还有把一个json转化为参数值的功能，和把参数值转化为json的功能</p>
 * <p>
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
     *
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


        public ParamItem(String paramKey, Class paramClass) {
            super(paramClass, paramKey);
        }

        protected void onReceiveKeyValue(RequestResponseBuilder requestResponseBuilder, String key, Object value) {
            requestResponseBuilder.putValue(key, value);
        }

        protected JSONObject getJson(RequestResponseBuilder requestResponseBuilder) {
            return requestResponseBuilder.getValues();
        }

        @Override
        public Object convertJson2ParamValue(RequestResponseBuilder requestResponseBuilder) {
            if (requestResponseBuilder == null || requestResponseBuilder.getValues() == null) {
                return null;
            }
            JSONObject jsonObject = getJson(requestResponseBuilder);
            if (jsonObject != null) {
                if (!isObjectDirectPut2Json(paramType)) {
                    try {
                        JSONObject value = !TextUtils.isEmpty(paramKey) ? (JSONObject) jsonObject.opt(paramKey) : jsonObject;
                        if (value == null) {
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
            if (!isObjectDirectPut2Json(obj)) {
                JSONObject json = convertObjectFileds2Json(obj);
                if (json == null) {
                    return;
                }
                if (!TextUtils.isEmpty(paramKey)) {
                    onReceiveKeyValue(requestResponseBuilder, paramKey, json);
                } else {
                    Iterator<String> iterator = json.keys();
                    String key = null;
                    while (iterator.hasNext()) {
                        key = iterator.next();
                        onReceiveKeyValue(requestResponseBuilder, key, json.opt(key));
                    }
                }

            } else {
                onReceiveKeyValue(requestResponseBuilder, paramKey, obj);

            }
        }

        private JSONObject convertObjectFileds2Json(Object obj) {
            JSONObject objectParamJson = null;


            Class cl = obj.getClass();
            Field[] fields = cl.getDeclaredFields();

            /*说明当前类的不包含任何属性*/
            if (fields.length == 0) {
                return objectParamJson;
            }

            Object inst = null;
            String jsonName = null;
            /*属性用Param进行了标注*/
            Param filedAnnoByParam = null;
            for (Field field : fields
                    ) {
            /*final或static类型的属性或枚举类型中的枚举常量不解析*/
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers()) || field.isEnumConstant()) {
                    continue;
                }
                filedAnnoByParam = field.getAnnotation(Param.class);
                if (filedAnnoByParam != null) {
                    jsonName = filedAnnoByParam.value();                                                    /*可以访问不可以访问的变量*/
                } else {
                    jsonName = field.getName();
                }

                field.setAccessible(true);
                try {
                    inst = field.get(obj);
                    if (inst != null) {
                        if (objectParamJson == null) {
                            objectParamJson = new JSONObject();
                        }

                        if (isObjectDirectPut2Json(inst)) {
                            objectParamJson.put(jsonName, inst);
                        } else {
                        /*检查当前的属性是否还包含着属性*/
                            JSONObject filedJson = convertObjectFileds2Json(inst);
                            if (filedJson != null) {
                                objectParamJson.put(jsonName, filedJson);
                            }
                        }


                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return objectParamJson.length() == 0 ? null : objectParamJson;

        }

        /**
         * 该对象是否可以直接往json中放
         *
         * @param type
         * @return
         */
        private boolean isObjectDirectPut2Json(Class type) {
            return (type == String.class || type.isPrimitive() || type == JSONArray.class || type == JSONObject.class);
        }

        /**
         * 该对象是否可以直接往json中放
         *
         * @param object
         * @return
         */
        private boolean isObjectDirectPut2Json(Object object) {
            if (object instanceof String || object instanceof Integer || object instanceof Double || object instanceof Long ||
                    object instanceof Boolean || object instanceof JSONArray || object instanceof JSONObject) {
                return true;
            }
            return false;
        }


    }


    /**
     * 对应{@link ParamResponseStatus}注解标注的参数
     */
    private static class ParamResponseStatusItem extends ParamItem {

        public ParamResponseStatusItem(Class paramClass, String paramKey) {
            super(paramKey, paramClass);
        }

        @Override
        protected JSONObject getJson(RequestResponseBuilder requestResponseBuilder) {
            return requestResponseBuilder.getResponseStatus();
        }

        @Override
        protected void onReceiveKeyValue(RequestResponseBuilder requestResponseBuilder, String key, Object value) {
            requestResponseBuilder.putResponseStatus(key, value);
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
        public Object convertJson2ParamValue(RequestResponseBuilder requestResponseBuilder) {
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
            requestResponseBuilder.setRequestCallback((IJavaCallback2JS) obj);

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
                    if (annotations[i].length == 0) {
                        throw new IllegalArgumentException("方法的所有参数必须都得用" + Param.class.getSimpleName() + "," + ParamCallback.class.getSimpleName() + "," + ParamResponseStatus.class.getSimpleName() + " 中的任意一个注解进行标注");

                    }
                    for (int j = 0; j < annotations[i].length; j++) {
                        annotation = annotations[i][j];
                        if (annotation != null && annotation instanceof Param) {
                            Param paramKey = (Param) annotation;
                            paramItem = new ParamItem(paramKey.value(), parameters[i]);
                            params.mParamItems[i] = paramItem;
                        } else if (annotation instanceof ParamCallback) {
                            paramItem = new ParamCallbackItem(parameters[i], null);
                            params.mParamItems[i] = paramItem;
                        } else if (annotation instanceof ParamResponseStatus) {
                            ParamResponseStatus paramResponseStatus = (ParamResponseStatus) annotation;
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
