package io.github.reinershir.auth.core.integrate.generator;

import org.springframework.jdbc.core.JdbcTemplate;

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
						+ "  `CREATE_DATE` datetime(0) NOT NULL,"
						+ "  `UPDATE_DATE` datetime(0) NULL DEFAULT NULL,"
						+ "  PRIMARY KEY (`ID`) USING BTREE"
						+ ") ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic"
						+ ";");
				
				generateSql.append("CREATE TABLE IF NOT EXISTS ");
				generateSql.append(tableName);
				generateSql.append("_PERMISSION");
				generateSql.append(" (  `ID` bigint(0) NOT NULL AUTO_INCREMENT,"
						+ "  `ROLE_ID` bigint(0) NOT NULL,"
						+ "  `MENU_ID` bigint(0) NOT NULL,"
						+ "  `PERMISSION_CODES` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,"
						+ "  PRIMARY KEY (`ID`) USING BTREE,"
						+ "  INDEX `ROLE_ID_INDEX`(`ROLE_ID`) USING BTREE,"
						+ "  INDEX `MENU_ID_INDEX`(`MENU_ID`) USING BTREE"
						+ ") ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic"
						+ "");
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
						+ ") COMMENT = '角色表'");
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
