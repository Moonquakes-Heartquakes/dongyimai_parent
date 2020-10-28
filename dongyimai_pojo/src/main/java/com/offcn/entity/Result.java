package com.offcn.entity;

import java.io.Serializable;

/**
 * @Auther: ysp
 * @Date: 2020/9/22 14:37
 * @Description: 更新操作返回信息的封装类
 */
public class Result implements Serializable {

    private String message;   //返回信息
    private boolean success;  //成功标识位

    public Result(){}

    public Result(boolean success,String message) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
