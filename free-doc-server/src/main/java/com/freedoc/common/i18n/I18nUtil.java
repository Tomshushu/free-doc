package com.freedoc.common.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class I18nUtil {

    private static MessageSource messageSource;

    public I18nUtil(MessageSource messageSource) {
        I18nUtil.messageSource = messageSource;
    }

    public static String getMessage(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key;
        }
    }

    public static String getMessage(String key, Locale locale, Object... args) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (NoSuchMessageException e) {
            return key;
        }
    }
}
