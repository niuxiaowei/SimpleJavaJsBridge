package com.bridge.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


import com.bridge.R;
import com.bridge.jsbridge.IJavaCallback2JS;
import com.bridge.jsbridge.SimpleJavaJsBridge;
import com.bridge.jsbridge.annotation.ParamResponseStatus;
import com.bridge.view.JavaInterfaces4JS;
import com.bridge.view.IInvokeJS;
import com.bridge.jsbridge.annotation.JavaCallback4JS;
import com.bridge.jsbridge.annotation.Param;


/**
 * Created by niuxiaowei on 16/5/25.
 */
public class WebViewFragment extends Fragment {

    public static final String WEBVIEW_URL = "webview_url";

    private WebView mWebView;
    private String mUrl;
    private TextView mResultView;

    private SimpleJavaJsBridge mSimpleJavaJsBridge;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_webview, container, false);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        parseArguments();
        initView();
        initData();
        super.onViewCreated(view, savedInstanceState);
    }

    public static Bundle createBundle(String url) {
        Bundle args = new Bundle();
        args.putString(WEBVIEW_URL, url);

        return args;
    }

    public static WebViewFragment createWebViewFragment(Bundle args) {
        WebViewFragment instance = new WebViewFragment();
        instance.setArguments(args);
        return instance;
    }

    public static WebViewFragment createWebViewFragment(String url) {
        return createWebViewFragment(createBundle(url));
    }

    protected void parseArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        mUrl = args.getString(WEBVIEW_URL);

    }

    public void initData() {
        JavaInterfaces4JS javaInterfaces4JS = new JavaInterfaces4JS(this);
        mSimpleJavaJsBridge = new SimpleJavaJsBridge.Builder().addJavaInterface4JS(javaInterfaces4JS)
                .setWebView(mWebView)
                .setJSMethodName4Java("_JSNativeBridge._handleMessageFromNative")
                .setProtocol("niu","receive_msg").create();
    }

    private void showLoading(){
        mResultView.setText("正在调用js的接口......");
    }

    public void setResult(String result){
        if(result != null){
            mResultView.setText(result);
        }
    }

    public void initView() {
        initWebView();

        mResultView = (TextView)getView().findViewById(R.id.result);
        getView().findViewById(R.id.invoke_js_exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                IInvokeJS invokeJS = mSimpleJavaJsBridge.createInvokJSCommand(IInvokeJS.class);
                invokeJS.exam("你好啊js", 7,new IJavaCallback2JS() {

                    @JavaCallback4JS
                    public void test(@ParamResponseStatus("msg")String statusMsg,@Param("msg") String msg) {
                        mResultView.setText(" 状态信息="+statusMsg+"  msg="+msg);
                    }

                });
            }
        });

        getView().findViewById(R.id.invoke_js_exam1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                IInvokeJS invokeJS = mSimpleJavaJsBridge.createInvokJSCommand(IInvokeJS.class);
                IInvokeJS.City city = new IInvokeJS.City();
                city.cityId = 10;
                city.cityName = "长治";
                city.cityProvince="山西";
                invokeJS.exam1(city, new IJavaCallback2JS() {

                    @JavaCallback4JS
                    public void test(@Param(needConvert = true) IInvokeJS.City city1) {
                        mResultView.setText("js返回信息： cityName="+city1.cityName+"  cityProvince="+city1.cityProvince);
                    }

                });
            }
        });
        getView().findViewById(R.id.invoke_js_exam2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                IInvokeJS invokeJS = mSimpleJavaJsBridge.createInvokJSCommand(IInvokeJS.class);
                IInvokeJS.City city = new IInvokeJS.City();
                city.cityId = 10;
                city.cityName = "长治";
                city.cityProvince="山西";
                invokeJS.exam2(city,"中国", new IJavaCallback2JS() {

                    @JavaCallback4JS
                    public void test(@Param(value = "city",needConvert = true) IInvokeJS.City city1) {
                        mResultView.setText("js返回信息： cityName="+city1.cityName+"  cityProvince="+city1.cityProvince);
                    }

                });
            }
        });
        getView().findViewById(R.id.invoke_js_exam3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                IInvokeJS invokeJS = mSimpleJavaJsBridge.createInvokJSCommand(IInvokeJS.class);
                IInvokeJS.City city = new IInvokeJS.City();
                city.cityId = 10;
                city.cityName = "长治";
                city.cityProvince="山西";
                invokeJS.exam3(city,"中国", new IJavaCallback2JS() {

                    @JavaCallback4JS
                    public void test(@Param(needConvert = true) IInvokeJS.City city1) {
                        mResultView.setText("js返回信息： cityName="+city1.cityName+"  cityProvince="+city1.cityProvince);
                    }

                });
            }
        });

        getView().findViewById(R.id.invoke_js_exam4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                IInvokeJS invokeJS = mSimpleJavaJsBridge.createInvokJSCommand(IInvokeJS.class);

                invokeJS.exam4( new IJavaCallback2JS() {

                    @JavaCallback4JS
                    public void test(@ParamResponseStatus("status") String status, @ParamResponseStatus("msg") String statusMsg) {
                        mResultView.setText("js返回信息： status="+status+"  statusMsg="+statusMsg);
                    }

                });
            }
        });


    }

    private void initWebView() {
        mWebView = (WebView) getView().findViewById(R.id.webView);

        mWebView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                mWebViewCallBack.shouldOverrideUrlLoading(view, url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

            }


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
//                mWebViewCallBack.onPageStarted(view, url, favicon);

            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }
        });


        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setBuiltInZoomControls(true);
        settings.setBlockNetworkImage(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setLoadsImagesAutomatically(true);

        // settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

        /* 解决空白页问题 */
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);


        if (!TextUtils.isEmpty(mUrl)) {
            mWebView.loadUrl(mUrl);
        }
    }


}
