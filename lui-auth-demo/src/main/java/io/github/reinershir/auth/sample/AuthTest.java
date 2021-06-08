package io.github.reinershir.auth.sample;

import io.github.reinershir.auth.annotation.EnableAuthentication;
import io.github.reinershir.auth.autoconfigure.WebMvcConfiguration;
import io.github.reinershir.auth.interceptor.AuthenticationInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableAuthentication
public class AuthTest {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(AuthTest.class, args);
        System.out.println("11111111111111111");
        System.out.println(run.getBean(WebMvcConfiguration.class));
        System.out.println(run.getBean(AuthenticationInterceptor.class));
    }
}
