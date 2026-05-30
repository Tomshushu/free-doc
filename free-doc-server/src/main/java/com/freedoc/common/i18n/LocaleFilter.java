package com.freedoc.common.i18n;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LocaleFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String acceptLanguage = request.getHeader("Accept-Language");
        Locale locale = resolveLocale(acceptLanguage);
        LocaleContextHolder.setLocale(locale);
        try {
            filterChain.doFilter(request, response);
        } finally {
            LocaleContextHolder.resetLocaleContext();
        }
    }

    private Locale resolveLocale(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.isEmpty()) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag(acceptLanguage.replace("_", "-"));
    }
}
