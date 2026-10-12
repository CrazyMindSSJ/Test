package cl.duoc.productos_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiRequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(ApiRequestLoggingInterceptor.class);
    private static final String START_TIME_ATTRIBUTE =
            ApiRequestLoggingInterceptor.class.getName() + ".startTime";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        request.setAttribute(START_TIME_ATTRIBUTE, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        Object startTimeAttribute = request.getAttribute(START_TIME_ATTRIBUTE);
        if (!(startTimeAttribute instanceof Long startTime)) {
            return;
        }

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        logger.info("{} {} -> {} ({} ms)",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs);
    }
}
