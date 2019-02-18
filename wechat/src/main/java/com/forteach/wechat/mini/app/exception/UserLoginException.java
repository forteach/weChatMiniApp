package com.forteach.wechat.mini.app.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: zhangyy
 * @email: zhang10092009@hotmail.com
 * @date: 19-2-18 11:40
 * @version: 1.0
 * @description:
 */
@Slf4j
public class UserLoginException extends RuntimeException{
    public UserLoginException() {
    }

    public UserLoginException(String message) {
        super(message);
        log.error("UserLoginException ==> message : {}", message);
    }

    public UserLoginException(String message, Throwable cause) {
        super(message, cause);
        log.error("UserLoginException ==> message : {}, cause : {}", message, cause);
    }

    public UserLoginException(Throwable cause) {
        super(cause);
        log.error("UserLoginException ==> cause : {}", cause);
    }

    public UserLoginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        log.error("UserLoginException ==> message : {}, cause : {}, enableSuppression : {}, writableStackTrace : {}", message, cause, enableSuppression, writableStackTrace);
    }
}
