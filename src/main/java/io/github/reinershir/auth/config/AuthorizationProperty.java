package io.github.reinershir.auth.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.jdbc.core.JdbcTemplate;

import io.github.reinershir.auth.config.property.AuthrizationConfig;
import io.github.reinershir.auth.config.property.IntegrateConfig;
import io.github.reinershir.auth.core.integrate.GenerateTable;
import io.github.reinershir.auth.core.integrate.generator.MenuGenerator;
import io.github.reinershir.auth.core.integrate.generator.RoleGenerator;

@ConfigurationProperties(prefix = "lui-auth")
public class AuthorizationProperty implements InitializingBean{
	JdbcTemplate jdbcTemplate;
	public AuthorizationProperty(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate=jdbcTemplate;
	}
	
	@NestedConfigurationProperty
	private AuthrizationConfig authrizationConfig;
	
	@NestedConfigurationProperty
	private IntegrateConfig intergrateConfig;
	
	
	public AuthrizationConfig getAuthrizationConfig() {
		return authrizationConfig;
	}

	public void setAuthrizationConfig(AuthrizationConfig authrizationConfig) {
		this.authrizationConfig = authrizationConfig;
	}

	public IntegrateConfig getIntergrateConfig() {
		return intergrateConfig;
	}

	public void setIntergrateConfig(IntegrateConfig intergrateConfig) {
		this.intergrateConfig = intergrateConfig;
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if(intergrateConfig!=null) {
			if(intergrateConfig.getEnable()) {
				GenerateTable roleGenerate = new RoleGenerator(jdbcTemplate);
				roleGenerate.generate(intergrateConfig.getRoleTableName());
				GenerateTable menuGenerate = new MenuGenerator(jdbcTemplate);
				menuGenerate.generate(intergrateConfig.getMenuTableName());
				
			}
		}
		
	}
}
