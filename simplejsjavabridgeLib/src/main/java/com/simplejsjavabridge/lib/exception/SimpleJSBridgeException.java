package com.simplejsjavabridge.lib.exception;

/**
 * Created by niuxiaowei on 16/9/10.
 */
public class SimpleJSBridgeException extends RuntimeException {

    public SimpleJSBridgeException() {
    }

    public SimpleJSBridgeException(String detailMessage) {
        super(detailMessage);
    }
}
