package io.github.reinershir.auth.utils;

import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.annotation.Permission;
import io.github.reinershir.auth.contract.DbContract;

public class CheckValueUtil {

	public static boolean checkPermissionCode(Permission hasPermission) {
		return hasPermission!=null&&StringUtils.hasText(hasPermission.value().toString())?true:false;
	}
	
	public static String getDbType(JdbcTemplate jdbcTemplate) throws SQLException {
		String dbType = null;
		String productName = null;
		productName = jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
		if(StringUtils.hasText(productName)&&productName.toUpperCase().indexOf(DbContract.DB_TYPE_MYSQL)!=-1) {
			dbType = DbContract.DB_TYPE_MYSQL;
		}else if(StringUtils.hasText(productName)&&productName.toUpperCase().indexOf(DbContract.DB_TYPE_ORACAL)!=-1) {
			dbType = DbContract.DB_TYPE_ORACAL;
		}else if(StringUtils.hasText(productName)&&productName.toUpperCase().indexOf(DbContract.DB_TYPE_POSTGRE)!=-1) {
			dbType = DbContract.DB_TYPE_POSTGRE;
		}
		return dbType;
	}
}
