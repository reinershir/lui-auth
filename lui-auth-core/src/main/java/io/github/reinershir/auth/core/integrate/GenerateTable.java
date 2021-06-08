package io.github.reinershir.auth.core.integrate;

import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.reinershir.auth.utils.CheckValueUtil;

public abstract class GenerateTable {
	
	protected JdbcTemplate jdbcTemplate;
	
	private String dbType;
	
	public GenerateTable(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate=jdbcTemplate;
	}

	/**
	 * @Title: generate
	 * @Description:   根据表名自动判断数据库生成表
	 * @author reinershir
	 * @date 2020年11月27日
	 * @param tableName
	 */
	public abstract void generate(String tableName);
	
	public String getDbType() {
		String productName = null;
		try {
			if(dbType ==null) {
				this.dbType=CheckValueUtil.getDbType(jdbcTemplate);
			}
			return this.dbType;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("unsupported database type : "+productName);
	}
}
