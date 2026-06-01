package com.freedoc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freedoc.common.i18n.I18nUtil;
import com.freedoc.common.result.R;
import com.freedoc.common.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
            try {
                String userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);

                UserPrincipal principal = new UserPrincipal(userId, username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                sendAuthError(request, response, e);
                return;
            }
        } else if (StringUtils.hasText(token)) {
            try {
                jwtUtil.parseToken(token);
            } catch (ExpiredJwtException e) {
                sendAuthError(request, response, e);
                return;
            } catch (Exception e) {
                sendAuthError(request, response, e);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendAuthError(HttpServletRequest request, HttpServletResponse response, Exception e) throws IOException {
        String acceptLanguage = request.getHeader("Accept-Language");
        Locale locale = acceptLanguage != null && !acceptLanguage.isEmpty()
                ? Locale.forLanguageTag(acceptLanguage.replace("_", "-"))
                : Locale.ENGLISH;

        boolean expired = e instanceof ExpiredJwtException;
        String messageKey = expired ? "error.auth.tokenExpired" : "error.auth.tokenInvalid";
        int code = expired ? 4011 : 4012;

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(
                R.fail(code, I18nUtil.getMessage(messageKey, locale))));
    }

}
