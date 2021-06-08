package io.github.reinershir.auth.core.integrate.generator;

import org.springframework.jdbc.core.JdbcTemplate;

import io.github.reinershir.auth.contract.DbContract;
import io.github.reinershir.auth.core.integrate.GenerateTable;

public class MenuGenerator extends GenerateTable{
	
	
	public MenuGenerator(JdbcTemplate jdbcTemplate) {
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
			//String tbName = jdbcTemplate.queryForRowSet(checkMysql).getString(1);
			if(!jdbcTemplate.queryForRowSet(checkMysql).first()) {
				generateSql.append("CREATE TABLE IF NOT EXISTS "+tableName);
				generateSql.append(" (  `ID` bigint(0) NOT NULL AUTO_INCREMENT,"
						+ "  `NAME` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '菜单名称',"
						+ "  `URL` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '跳转地址',"
						+ "  `ICON` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图标',"
						+ "  `PERMISSION_CODES` varchar(150) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '标识权限码',"
						+ "  `DESCRIPTION` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,"
						+ "  `LEFT_VALUE` int(0) NOT NULL COMMENT '左节点值',"
						+ "  `RIGHT_VALUE` int(0) NOT NULL COMMENT '右节点值',"
						+ "  `LEVEL` int(0) NOT NULL COMMENT '节点等级',"
						+ "  `PROPERTY` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '属性(自由使用标识)',"
						+ "  `CREATE_DATE` datetime(0) NOT NULL,"
						+ "  `UPDATE_DATE` datetime(0) NULL DEFAULT NULL,"
						+ "  PRIMARY KEY (`ID`) USING BTREE"
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
						+ "    ID int NOT NULL AUTO_INCREMENT COMMENT 'ID',"
						+ "    NAME varchar(100) NOT NULL ,"
						+ "    URL varchar(200) NOT NULL ,"
						+ "    ICON varchar(80) NOT NULL ,"
						+ "    PERMISSION_CODES varchar(150) NOT NULL ,"
						+ "    DESCRIPTION varchar(200) NOT NULL COMMENT '说明',"
						+ "    LEFT_VALUE  number(10) NOT NULL COMMENT '左节点值',"
						+ "    RIGHT_VALUE  number(10) NOT NULL COMMENT '右节点值',"
						+ "    LEVEL  number(10) NOT NULL COMMENT '节点等级',"
						+ "    PROPERTY varchar(200) NOT NULL ,"
						+ "    CREATE_DATE datetime NOT NULL COMMENT '创建时间',"
						+ "    UPDATE_DATE datetime COMMENT '更新时间',"
						+ "    PRIMARY KEY (id)"
						+ ") COMMENT = '菜单表'");
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
