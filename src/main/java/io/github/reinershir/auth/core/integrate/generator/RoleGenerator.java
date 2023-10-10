package io.github.reinershir.auth.core.integrate.generator;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import io.github.reinershir.auth.contract.DbContract;
import io.github.reinershir.auth.core.integrate.GenerateTable;

/**
 * 生成角色表
 * @author reinershir
 */
public class RoleGenerator extends GenerateTable{
	
	
	public RoleGenerator(JdbcTemplate jdbcTemplate) {
		super(jdbcTemplate);
	}

	@Override
	public void generate(String tableName) {
		StringBuilder generateSql = new StringBuilder();
		boolean tableExists = false;
		String dbType = super.getDbType();
		switch(dbType) {
		case DbContract.DB_TYPE_MYSQL:
			String checkMysql = "SHOW TABLES LIKE '"+tableName+"'";
			if(!jdbcTemplate.queryForRowSet(checkMysql).first()) {  
				generateSql.append("CREATE TABLE IF NOT EXISTS "+tableName);
				generateSql.append(" (  `ID` bigint(0) NOT NULL AUTO_INCREMENT,"
						+ "  `ROLE_NAME` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',"
						+ "  `DESCRIPTION` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,"
						+ "  `CREATE_DATE` datetime NOT NULL,"
						+ "  `UPDATE_DATE` datetime NULL DEFAULT NULL,"
						+ "  PRIMARY KEY (`ID`) USING BTREE"
						+ ") ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic"
						+ ";");
				
				generateSql.append("CREATE TABLE IF NOT EXISTS ");
				generateSql.append(tableName);
				generateSql.append("_PERMISSION");
				generateSql.append(" (  `ID` bigint(0) NOT NULL AUTO_INCREMENT,"
						+ "  `ROLE_ID` bigint(0) NOT NULL,"
						+ "  `MENU_ID` bigint(0) NOT NULL,"
						+ "  `PERMISSION_CODES` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci,"
						+ "  PRIMARY KEY (`ID`) USING BTREE,"
						+ "  INDEX `ROLE_ID_INDEX`(`ROLE_ID`) USING BTREE,"
						+ "  INDEX `MENU_ID_INDEX`(`MENU_ID`) USING BTREE"
						+ ") ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic"
						+ ";");
				
				generateSql.append("CREATE TABLE IF NOT EXISTS ");
				generateSql.append(tableName);
				generateSql.append("_USER");
				generateSql.append(" (  `ID` bigint(0) NOT NULL AUTO_INCREMENT,"
						+ "  `ROLE_ID` bigint(0) NOT NULL,"
						+ "  `USER_ID` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
						+ "  PRIMARY KEY (`ID`) USING BTREE"
						+ ") ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic"
						+ ";");
			}else {
				tableExists = true;
			}
			break;
		case DbContract.DB_TYPE_ORACAL:
			String checkOracle = "SELECT COUNT(1) FROM "+tableName+" WHERE ROWNUM = 1;";
			if(!jdbcTemplate.queryForRowSet(checkOracle).first()) {
				generateSql.append("CREATE TABLE IF NOT EXISTS "+tableName);
				generateSql.append(" ("
						+ "    ID number(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',"
						+ "    ROLE_NAME varchar(100) NOT NULL COMMENT '角色名称',"
						+ "    DESCRIPTION varchar(200) NOT NULL COMMENT '说明',"
						+ "    CREATE_DATE datetime NOT NULL COMMENT '创建时间',"
						+ "    UPDATE_DATE datetime COMMENT '更新时间',"
						+ "    PRIMARY KEY (id)"
						+ ") COMMENT = '角色表';");
				
				generateSql.append("CREATE TABLE IF NOT EXISTS ");
				generateSql.append(tableName);
				generateSql.append("_PERMISSION");
				generateSql.append(" (  `ID` number(20) NOT NULL AUTO_INCREMENT,"
						+ "  `ROLE_ID` number(20) NOT NULL,"
						+ "  `MENU_ID` number(20) NOT NULL,"
						+ "  `PERMISSION_CODES` varchar(150) NOT NULL,"
						+ "  PRIMARY KEY (`ID`),"
						+ ") COMMENT = '角色权限表';");
				
				
				generateSql.append("CREATE TABLE IF NOT EXISTS ");
				generateSql.append(tableName);
				generateSql.append("_USER");
				generateSql.append(" (  `ID` number(20) NOT NULL AUTO_INCREMENT,"
						+ "  `ROLE_ID` number(20) NOT NULL,"
						+ "  `USER_ID` varchar(150) NOT NULL"
						+ "  PRIMARY KEY (`ID`),"
						+ ") COMMENT = '用户角色关系表';");
			}else {
				tableExists = true;
			}
			break;
		case DbContract.DB_TYPE_POSTGRE:
			String checkPostgre = "select count(*) from pg_class where relname = '"+tableName.toLowerCase()+"';";
			SqlRowSet rowset = jdbcTemplate.queryForRowSet(checkPostgre);
			if(rowset.next()&&rowset.getInt("count")<1) {
				generateSql.append("CREATE TABLE public.");
				generateSql.append(tableName);
				generateSql.append(" (\n"
						+ "  ID serial4 NOT NULL ,\n"
						+ "  ROLE_NAME varchar(100) COLLATE pg_catalog.default NOT NULL,\n"
						+ "  DESCRIPTION varchar(200) COLLATE pg_catalog.default,\n"
						+ "  CREATE_DATE date NOT NULL,\n"
						+ "  UPDATE_DATE date,\n"
						+ "  PRIMARY KEY (ID)\n"
						+ ")\n"
						+ ";");
				
				generateSql.append("CREATE TABLE public."+tableName+"_USER (\n"
						+ " ID serial4,"
						+ "  ROLE_ID int8 NOT NULL,\n"
						+ "  USER_ID varchar COLLATE pg_catalog.default NOT NULL\n"
						+ ");");
				
				generateSql.append("CREATE TABLE public."+tableName+"_PERMISSION (\n"
						+ "  ID serial4,\n"
						+ "  ROLE_ID int8 NOT NULL,\n"
						+ "  MENU_ID int8 NOT NULL,\n"
						+ "  PERMISSION_CODES varchar(150),\n"
						+ "  PRIMARY KEY (ID)\n"
						+ ")\n"
						+ ";");
			}else {
				tableExists = true;
			}
			break;
		}
		if(!tableExists) {
			jdbcTemplate.update(generateSql.toString());
		}
		
	}

}
