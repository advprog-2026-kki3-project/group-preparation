package id.ac.ui.cs.advprog.bidmart.auth.config;

import id.ac.ui.cs.advprog.bidmart.auth.security.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
    private final PermissionInterceptor permissionInterceptor;

    public WebMvcConfiguration(PermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/catalog").setViewName("forward:/index.html");
        registry.addViewController("/catalog/**").setViewName("forward:/index.html");
        registry.addViewController("/wallet").setViewName("forward:/index.html");
        registry.addViewController("/security").setViewName("forward:/index.html");
        registry.addViewController("/sessions").setViewName("forward:/index.html");
        registry.addViewController("/admin").setViewName("forward:/index.html");
        registry.addViewController("/auctions/**").setViewName("forward:/index.html");
    }
}
