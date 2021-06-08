package io.github.reinershir.auth.autoconfigure;

import io.github.reinershir.auth.annotation.EnableAuthentication;
import io.github.reinershir.auth.interceptor.AuthenticationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(value = {EnableAuthentication.class, RedisAutoConfiguration.class})
public class LuiAuthAutoConfiguration {

    @ConditionalOnBean(AuthenticationInterceptor.class)
    @Import(WebMvcConfiguration.class)
    protected static class WebMvcConfig {
        protected WebMvcConfig() {
        }
    }
}
