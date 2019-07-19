package org.cyanspring.tools.common;

import java.io.Serializable;

/**
 * 请求的返回类型封装JSON结果
 */
public class BaseResult<E> implements Serializable {
    private static final long serialVersionUID = -4443339752032771087L;
    private int code;
    private String message;
    private E result;

    public BaseResult() {
    }

    public BaseResult(Message message, E result) {
        this.code = message.getCode();
        this.message = message.getText();
        this.result = result;
    }

    public BaseResult(Message message) {
        this.code = message.getCode();
        this.message = message.getText();
        this.result = null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public E getResult() {
        return result;
    }

    public void setResult(E result) {
        this.result = result;
    }
}
