package com.bridge.jsbridge;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * java与js之间可以进行互相通信，主动发起通信时，传输的数据我们称作request(请求数据)对应{@link Request},当把处理结果进行返回时的数据我们称作response(响应数据)
 * 对应{@link Response},因此该类的主要作用就是用来构建request或者response数据的。每次只能构建request或者response其中一种数据，
 * {@link RequestResponseBuilder}可以从json中解析出相应的数据，也可以转化为json
 *
 * Created by niuxiaowei on 16/9/14.
 */
public class RequestResponseBuilder {


    /**
     * 是否是构建request请求
     */
    private boolean mIsBuildRequest;

    /**
     * 请求数据
     */
    private Request mRequest;
    /**
     * 响应数据
     */
    private Response mResponse;

    public RequestResponseBuilder(boolean isBuildRequest){
        this(isBuildRequest,null);
    }

    /**
     * @param isBuildRequest 是否是构造请求数据
     * @param data json数据
     */
    public RequestResponseBuilder(boolean isBuildRequest, JSONObject data) {
        mIsBuildRequest = isBuildRequest;
        if (mIsBuildRequest) {
            mRequest = new Request();
            if(data != null){

                mRequest.parseRequest(data);
            }
        } else {
            mResponse = new Response();
            if(data != null){

                mResponse.parseResponse(data);
            }
        }
    }


    /**
     *
     * 请求数据格式：
     *
     * <pre>
     *    {
     *      "handlerName":"test",
     *      "callbackId":"c_111111",
     *      "params":{
     *          ....
     *      }
     *    }
     *
     *    hanlerName 代表java与js之间给对方暴漏的接口的名称，
     *    callbackId 代表对方在发起请求时，会为回调方法生产一个唯一的id值，它就代表这个唯一的id值
     *    params     代表传递的数据
     * </pre>
     * }
     */
    private static class Request {

        private static String sRequestInterfaceName = "handlerName";
        private static String sRequestCallbackIdName = "callbackId";
        private static String sRequestValuesName = "params";

        /*request相关的属性*/
        private String interfaceName;
        private String callbackId;
        private JSONObject requestValues;
        private IJavaCallback2JS iJavaCallback2JS;

        private static void init(String requestInterfaceName, String requestCallbackIdName, String requestValuesName) {
            if (!TextUtils.isEmpty(requestCallbackIdName)) {
                Request.sRequestCallbackIdName = requestCallbackIdName;
            }

            if (!TextUtils.isEmpty(requestValuesName)) {
                Request.sRequestValuesName = requestValuesName;
            }
            if (!TextUtils.isEmpty(requestInterfaceName)) {
                Request.sRequestInterfaceName = requestInterfaceName;
            }
        }

        private void parseRequest(JSONObject json) {
            if (json != null) {
                callbackId = json.optString(sRequestCallbackIdName);
                interfaceName = json.optString(sRequestInterfaceName);
                requestValues = json.optJSONObject(sRequestValuesName);
            }
        }

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(sRequestCallbackIdName, callbackId);
                jsonObject.put(sRequestInterfaceName, interfaceName);
                if (requestValues != null) {
                    jsonObject.put(sRequestValuesName, requestValues);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return "'" + jsonObject.toString() + "'";
        }
    }

    /**
     *
     * response数据格式：
     * <pre>
     *  {
     *      "responseId":"iii",
     *      "data":{
     *          "status":"1",
     *          "msg":"ok",
     *          "values":{
     *              ......
     *          }
     *      }
     *  }
     *
     *  responseId 代表request中的callbackId
     *  data       代表响应的数据
     *  status     代表响应状态
     *  msg        代表响应状态对应的消息
     *  values     代表响应数据包含的值
     * </pre>
     */
    private static class Response {
        private static String sResponseIdName = "responseId";
        private static String sResponseValuesName = "values";
        private static String sResponseName = "data";

        private String responseId;
        private JSONObject response = new JSONObject();
        private JSONObject responseValues;

        private static void init(String responseIdName, String responseName, String responseValuesName) {
            if (!TextUtils.isEmpty(responseValuesName)) {

                Response.sResponseValuesName = responseValuesName;
            }
            if (!TextUtils.isEmpty(responseIdName)) {
                Response.sResponseIdName = responseIdName;
            }

            if (!TextUtils.isEmpty(responseName)) {

                Response.sResponseName = responseName;
            }

        }

        private void parseResponse(JSONObject json) {
            if (json != null) {
                responseId = json.optString(sResponseIdName);
                response = json.optJSONObject(sResponseName);
                if (response != null) {
                    responseValues = response.optJSONObject(sResponseValuesName);
                }
            }
        }

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(sResponseIdName, responseId);
                if (responseValues != null) {
                    response.put(sResponseValuesName, responseValues);
                }
                jsonObject.put(sResponseName, response);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "'" + jsonObject.toString() + "'";
        }
    }

    /**
     * 获取请求时为回调函数生成的 callbackId
     * @return
     */
    public String getCallbackId(){
        return mRequest == null?null: mRequest.callbackId;
    }

    /**
     * 获取请求的接口的名字
     * @return
     */
    public String getInterfaceName() {
        return mRequest == null ? null : mRequest.interfaceName;
    }

    public void setRequestCallback(IJavaCallback2JS callback) {
        initRequest();
        this.mRequest.iJavaCallback2JS = callback;
    }

    private void initRequest() {
        if (mRequest == null) {
            mRequest = new Request();
        }
    }

    /**
     * 设置请求的接口的名字
     * @param interfaceName
     */
    public void setInterfaceName(String interfaceName) {
        initRequest();
        this.mRequest.interfaceName = interfaceName;
    }

    /**
     * 为回调方法设置回调id
     * @param callbackId
     */
    public void setCallbackId(String callbackId) {
        initRequest();
        this.mRequest.callbackId = callbackId;
    }


    /**
     * 获取request或者response的 values值
     * @return
     */
    public JSONObject getValues() {
        if (mIsBuildRequest) {
            return mRequest == null ? null : mRequest.requestValues;
        } else {
            return mResponse == null ? null : mResponse.responseValues;
        }
    }

    /**
     * 往request或者response中存放 数据
     * @param key
     * @param value
     */
    public void putValue(String key, Object value) {
        if(TextUtils.isEmpty(key) || value == null){
            return;
        }
        JSONObject values = null;
        if (mIsBuildRequest) {
            initRequest();
            if (mRequest.requestValues == null) {
                mRequest.requestValues = new JSONObject();
            }
            values = mRequest.requestValues;
        } else {
            initResponse();
            if (mResponse.responseValues == null) {
                mResponse.responseValues = new JSONObject();
            }
            values = mResponse.responseValues;
        }

        try {
            values.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public IJavaCallback2JS getCallback() {
        return mRequest == null ? null : mRequest.iJavaCallback2JS;
    }

    /**
     * @param responseIdName
     * @param responseName
     * @param responseValuesName
     */
    public static void init(String responseIdName, String responseName, String responseValuesName, String requestInterfaceName, String requestCallbackIdName, String requestValuesName) {
        Response.init(responseIdName, responseName, responseValuesName);
        Request.init(requestInterfaceName, requestCallbackIdName, requestValuesName);
    }


    private void initResponse() {
        if (mResponse == null) {
            mResponse = new Response();
        }
    }

    public String getResponseId() {
        return mResponse == null ? null : mResponse.responseId;
    }

    public void setResponseId(String responseId) {
        initResponse();
        this.mResponse.responseId = responseId;
    }

    /**
     * 获取response的 状态数据
     * @return
     */
    public JSONObject getResponseStatus() {
        return mResponse == null ? null : mResponse.response;
    }

    /**
     * 往response中存放 数据
     * @param key
     * @param value
     */
    public void putResponseStatus(String key, Object value) {
        if(TextUtils.isEmpty(key) || value == null){
            return;
        }
        initResponse();
        try {
            mResponse.response.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 从json中创建一个{@link RequestResponseBuilder}对象，其实最终创建的是一个 request或者response
     * @param json
     * @return
     */
     static RequestResponseBuilder create(JSONObject json) {
        if (json == null) {
            return null;
        }
        RequestResponseBuilder requestResponseBuilder = null;
        /*响应数据*/
        if (json.has(Response.sResponseIdName)) {
            requestResponseBuilder = new RequestResponseBuilder(false, json);
        } else {
            requestResponseBuilder = new RequestResponseBuilder(true, json);
        }

        return requestResponseBuilder;
    }

    /**
     * 是否构建的时request数据
     * @return
     */
    public boolean isBuildRequest() {
        return mIsBuildRequest;
    }

    @Override
    public String toString() {
        if (mIsBuildRequest) {
            return mRequest == null ? super.toString() : mRequest.toString();
        } else {
            return mResponse == null ? super.toString() : mResponse.toString();
        }
    }
}
