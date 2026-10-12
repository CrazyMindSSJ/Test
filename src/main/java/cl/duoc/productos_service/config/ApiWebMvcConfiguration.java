package cl.duoc.productos_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiWebMvcConfiguration implements WebMvcConfigurer {
    private final ApiRequestLoggingInterceptor apiRequestLoggingInterceptor;

    public ApiWebMvcConfiguration(ApiRequestLoggingInterceptor apiRequestLoggingInterceptor) {
        this.apiRequestLoggingInterceptor = apiRequestLoggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiRequestLoggingInterceptor)
                .addPathPatterns("/api/**");
    }
}
 