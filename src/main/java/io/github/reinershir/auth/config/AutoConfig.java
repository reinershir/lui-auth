package io.github.reinershir.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.reinershir.auth.core.CustomManager;
import io.github.reinershir.auth.core.integrate.access.MenuAccess;
import io.github.reinershir.auth.core.integrate.access.RoleAccess;
import io.github.reinershir.auth.core.security.reqlog.DefaultRequestLogger;
import io.github.reinershir.auth.core.security.reqlog.RequestLogger;
import io.github.reinershir.auth.core.support.Appointor;
import io.github.reinershir.auth.core.support.AuthorizeManager;
import io.github.reinershir.auth.interceptor.AuthenticationInterceptor;
import io.github.reinershir.auth.interceptor.RequestFilter;

@Configuration
@EnableConfigurationProperties(AuthorizationProperty.class)
public class AutoConfig {

	AuthorizationProperty property;
	StringRedisTemplate redisTemplate;
	JdbcTemplate jdbcTemplate;
	
	public AutoConfig(AuthorizationProperty property,StringRedisTemplate redisTemplate,JdbcTemplate jdbcTemplate) {
		this.property=property;
		this.redisTemplate=redisTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	@Bean
	public AuthenticationInterceptor initAuthenticationInterceptor(@Autowired(required = false) RequestLogger requestLogger) {
		return new AuthenticationInterceptor(redisTemplate,property,initCustomManager(),initAuthorizeManager(),requestLogger);
	}
	
	
	@Bean
	public AuthorizeManager initAuthorizeManager() {
		RoleAccess roleAccess =null;
		MenuAccess menuAccess = null;
		if(property.getIntergrateConfig()!=null) {
			roleAccess = initRoleAccess();
			menuAccess = initMenuAccess();
		}
		Appointor appointor = new Appointor(redisTemplate, roleAccess);
		return new AuthorizeManager(property,redisTemplate,initScaner(),appointor,menuAccess);
	}
	
	@Bean
	@ConditionalOnProperty(name = "lui-auth.intergrateConfig.enable",havingValue = "true")
	public MenuAccess initMenuAccess() {
		String roleTableName = property.getIntergrateConfig().getRoleTableName();
		String menuTableName = property.getIntergrateConfig().getMenuTableName();
		return new MenuAccess(jdbcTemplate,menuTableName,roleTableName);
	}
	
	@Bean
	@ConditionalOnProperty(name = "lui-auth.intergrateConfig.enable",havingValue = "true")
	public RoleAccess initRoleAccess() {
		String roleTableName = property.getIntergrateConfig().getRoleTableName();
		String menuTableName = property.getIntergrateConfig().getMenuTableName();
		return new RoleAccess(jdbcTemplate,roleTableName,menuTableName,redisTemplate);
	}
	
	@Bean
	public CustomManager initCustomManager() {
		return new CustomManager();
	}
	
	@Bean
	public PermissionScanner initScaner() {
		return new PermissionScanner();
	}
	
	@Bean
	@ConditionalOnProperty(name = "lui-auth.securityConfig.enableRequestLog",havingValue = "true")
    public FilterRegistrationBean<RequestFilter> registerAuthFilter() {
        FilterRegistrationBean<RequestFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new RequestFilter());
        registration.addUrlPatterns("/*");
        registration.setName("requestFilter");
        registration.setOrder(1);   
        return registration;
    }
	
	@Bean
	@ConditionalOnMissingBean(RequestLogger.class)
	@ConditionalOnProperty(name = "lui-auth.securityConfig.enableRequestLog",havingValue = "true")
	public RequestLogger initRequestLogger() {
		return new DefaultRequestLogger();
	}
	
}
