package com.simplejsjavabridge.lib;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebStorage;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 *该类的主要作用是在{@link #onJsPrompt(WebView, String, String, String, JsPromptResult)}对js传递的数据交给{@link SimpleJavaJsBridge}进行
 * 处理 并且在{@link #onProgressChanged(WebView, int)}方法里把内嵌的js文件注入h5页面中，这样就省得使用者在关心这些环节了。
 * {@link #mWebChromeClient}的主要作用是使用者直接把自己生成的{@link WebChromeClient}传递进来，该类负责调用相应的方法
 */
public class SimpleJavaJSWebChromeClient extends WebChromeClient {


    private WebChromeClient mWebChromeClient;

    public boolean mIsInjectedJS;

    private SimpleJavaJsBridge mSimpleJavaJsBridge;

    SimpleJavaJSWebChromeClient(WebChromeClient webChromeClient, SimpleJavaJsBridge simpleJavaJsBridge) {
        mWebChromeClient = webChromeClient;
        mSimpleJavaJsBridge = simpleJavaJsBridge;
    }

    private static void webViewLoadLocalJs(WebView view, String path) {
        String jsContent = assetFile2Str(view.getContext(), path);
        view.loadUrl("javascript:" + jsContent);
    }

    private static String assetFile2Str(Context c, String urlStr) {
        InputStream in = null;
        try {
            in = c.getAssets().open(urlStr);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            StringBuilder sb = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                if (line != null) {
                    line = line.replaceAll("\\t", "   ");
                    if (!line.matches("^\\s*\\/\\/.*")) {
                        sb.append(line);
                    }
                }
            } while (line != null);

            bufferedReader.close();
            in.close();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {

        Log.i("test", "--new pro=" + newProgress);

//        if (newProgress <= 98) {
//            mIsInjectedJS = false;
//        } else if (!mIsInjectedJS) {
//            mIsInjectedJS = true;
//            webViewLoadLocalJs(view, "js_native_bridge.js");
//        }
//
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onProgressChanged(view, newProgress);
        }
        super.onProgressChanged(view, newProgress);


    }

    private boolean checkObjectNotNull(Object object) {
        return object != null;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//        result.cancel();
        if (checkObjectNotNull(mWebChromeClient)) {
            return mWebChromeClient.onJsAlert(view, url, message, result);
        }
        return super.onJsAlert(view, url, message, result);
    }


    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
        if (mSimpleJavaJsBridge.parseJsonFromJs(message)) {
            /*必须得有这行代码，否则会阻塞当前h5页面*/
            result.cancel();
            return true;
        }

        if (checkObjectNotNull(mWebChromeClient)) {
            return mWebChromeClient.onJsPrompt(view, url, message, defaultValue, result);
        }

        return super.onJsPrompt(view, url, message, defaultValue, result);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onReceivedTitle(view, title);
        }
        super.onReceivedTitle(view, title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onReceivedIcon(view, icon);
        }
        super.onReceivedIcon(view, icon);
    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onReceivedTouchIconUrl(view, url, precomposed);
        }
        super.onReceivedTouchIconUrl(view, url, precomposed);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onShowCustomView(view, callback);
        }
        super.onShowCustomView(view, callback);
    }

    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onShowCustomView(view, requestedOrientation, callback);
        }
        super.onShowCustomView(view, requestedOrientation, callback);
    }

    @Override
    public void onHideCustomView() {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onHideCustomView();
        }
        super.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        if (checkObjectNotNull(mWebChromeClient)) {
            return mWebChromeClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onRequestFocus(view);
        }
        super.onRequestFocus(view);
    }

    @Override
    public void onCloseWindow(WebView window) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onCloseWindow(window);
        }
        super.onCloseWindow(window);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        if (checkObjectNotNull(mWebChromeClient)) {
            return mWebChromeClient.onJsConfirm(view, url, message, result);
        }
        return super.onJsConfirm(view, url, message, result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
        if (checkObjectNotNull(mWebChromeClient)) {
            return mWebChromeClient.onJsBeforeUnload(view, url, message, result);
        }
        return super.onJsBeforeUnload(view, url, message, result);
    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
        }
        super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
        }
        super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
        }
        super.onGeolocationPermissionsShowPrompt(origin, callback);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onGeolocationPermissionsHidePrompt();
        }
        super.onGeolocationPermissionsHidePrompt();
    }

    @Override
    public void onPermissionRequest(PermissionRequest request) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onPermissionRequest(request);
        }
        super.onPermissionRequest(request);
    }

    @Override
    public void onPermissionRequestCanceled(PermissionRequest request) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onPermissionRequestCanceled(request);
        }
        super.onPermissionRequestCanceled(request);
    }

    @Override
    public boolean onJsTimeout() {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onJsTimeout();
        }
        return super.onJsTimeout();
    }

    @Override
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onConsoleMessage(message, lineNumber, sourceID);
        }
        super.onConsoleMessage(message, lineNumber, sourceID);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (checkObjectNotNull(mWebChromeClient)) {
            mWebChromeClient.onConsoleMessage(consoleMessage);
        }
        return super.onConsoleMessage(consoleMessage);
    }
}
