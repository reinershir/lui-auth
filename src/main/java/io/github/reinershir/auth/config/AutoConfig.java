package io.github.reinershir.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import io.github.reinershir.auth.core.support.AuthorizeManager;
import io.github.reinershir.auth.interceptor.AuthenticationInterceptor;

@Configuration
@EnableConfigurationProperties(AuthorizationProperty.class)
public class AutoConfig {

	AuthorizationProperty property;
	StringRedisTemplate redisTemplate;
	
	public AutoConfig(AuthorizationProperty property,StringRedisTemplate redisTemplate) {
		this.property=property;
		this.redisTemplate=redisTemplate;
	}
	
	@Bean
	public AuthenticationInterceptor initAuthenticationInterceptor() {
		return new AuthenticationInterceptor(redisTemplate,property,initCustomManager(),initAuthorizeManager());
	}
	
	@Bean
	public AuthorizeManager initAuthorizeManager() {
		return new AuthorizeManager(property,redisTemplate,initScaner());
	}
	
	@Bean
	public CustomManager initCustomManager() {
		return new CustomManager();
	}
	
	@Bean
	public PermissionScanner initScaner() {
		return new PermissionScanner();
	}
}
