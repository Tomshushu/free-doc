package com.freedoc.common.result;

import com.freedoc.common.i18n.I18nUtil;
import lombok.Data;

import java.io.Serializable;

@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;

    public R() {
    }

    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return ok(null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    public static <T> R<T> fail() {
        return fail(I18nUtil.getMessage("error.system.operationFailed"));
    }

    public static <T> R<T> fail(String messageKey) {
        return new R<>(500, I18nUtil.getMessage(messageKey), null);
    }

    public static <T> R<T> fail(int code, String messageKey) {
        return new R<>(code, I18nUtil.getMessage(messageKey), null);
    }

    public static <T> R<T> failWithArgs(int code, String messageKey, Object... args) {
        return new R<>(code, I18nUtil.getMessage(messageKey, args), null);
    }

    public boolean isSuccess() {
        return this.code == 200;
    }

}
