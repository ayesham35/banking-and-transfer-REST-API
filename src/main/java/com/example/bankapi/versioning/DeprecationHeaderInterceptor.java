package com.example.bankapi.versioning;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class DeprecationHeaderInterceptor implements HandlerInterceptor {

    private static final String SUNSET_DATE =
            "Wed, 01 Jul 2026 00:00:00 GMT";

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {

        String path = request.getRequestURI();

        if (path.startsWith("/api/v1")) {
            response.setHeader("Deprecation", "true");
            response.setHeader("Sunset", SUNSET_DATE);

            String successor = mapV1ToV2(path);
            if (successor != null) {
                response.setHeader("Link", "<" + successor + ">; rel=\"successor-version\"");
            }
        }
    }

    private String mapV1ToV2(String v1Path) {
        return v1Path.replace("v1", "v2");
    }
}
