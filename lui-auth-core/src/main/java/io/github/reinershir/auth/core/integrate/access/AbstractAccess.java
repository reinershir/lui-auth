package io.github.reinershir.auth.core.integrate.access;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.contract.DbContract;
import io.github.reinershir.auth.utils.CheckValueUtil;

public class AbstractAccess<T> {

	protected JdbcTemplate jdbcTemplate;
	protected String tableName;
	private String dbType;
	Class<T> clazz = null;
	
	@SuppressWarnings("unchecked")
	public AbstractAccess(JdbcTemplate jdbcTemplate,String tableName) {
		
		this.jdbcTemplate=jdbcTemplate;
		this.tableName=tableName;
		Type type = getClass().getGenericSuperclass();
        if( type instanceof ParameterizedType ){
            ParameterizedType pType = (ParameterizedType)type;
            Type claz = pType.getActualTypeArguments()[0];
            if( claz instanceof Class ){
                this.clazz = (Class<T>) claz;
            }
        }

	}
	
	protected String getDbType() {
		if(dbType==null) {
			try {
				this.dbType = CheckValueUtil.getDbType(jdbcTemplate);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return dbType;
	}
	
	protected List<T> qureyList(Integer page,Integer pageSize,String name,String fieldName,RowMapper<T> mapper){
		StringBuilder sql = new StringBuilder();
		switch(getDbType()) {
		case DbContract.DB_TYPE_MYSQL:
			sql.append("SELECT * FROM ");
			sql.append(tableName);
			if(!StringUtils.isEmpty(name)) {
				sql.append(" WHERE LOCATE(?, "+fieldName+") > 0 ");
			}
			sql.append(" ORDER BY CREATE_DATE DESC ");
			break;
		case DbContract.DB_TYPE_ORACAL:
			sql.append("SELECT ROWNUM,T.* FROM (SELECT * FROM ");
			sql.append(tableName);
			if(!StringUtils.isEmpty(name)) {
				sql.append(" WHERE INSTR("+fieldName+",?) > 0");
			}
			sql.append(" ORDER BY CREATE_DATE DESC ");
			sql.append(") as T WHERE ROWNUM >= ");
			sql.append((page-1)*pageSize);
			sql.append(" AND ROWNUM <= ");
			sql.append(page*pageSize);
			break;
		}
		
		List<T> list = null;
		if(!StringUtils.isEmpty(name)) {
			String[] param={name};
			list = jdbcTemplate.query(sql.toString(),param,mapper);
		}else {
			list = jdbcTemplate.query(sql.toString(), mapper);
		}
		return list;
	}
	
	protected Long selectCount(@Nullable String name,String filedName) {
		StringBuilder sql = new StringBuilder("SELECT COUNT(ID) FROM ");
		sql.append(tableName);
		Long count = null;
		if(!StringUtils.isEmpty(name)) {
			sql.append(" WHERE LOCATE(?, "+filedName+") > 0 ");
			count = jdbcTemplate.queryForObject(sql.toString(), Long.class,name);
		}else {
			count = jdbcTemplate.queryForObject(sql.toString(), Long.class);
		}
		return count;
	}
	
	protected int deleteById(@Nonnull Long id) {
		if(id!=null) {
			return jdbcTemplate.update("DELETE FROM "+tableName+" WHERE ID = "+id);
		}
		return -1;
	}
	
	protected T selectById(Long id,RowMapper<T> mapper) {
		try {
			return jdbcTemplate.queryForObject("SELECT * FROM "+tableName+" WHERE ID = ?", mapper,id);
		} catch (DataAccessException e) {
			return null;
		}
	}
	
	protected List<T> selectByList(List<Long> ids,RowMapper<T> mapper){
		if(!CollectionUtils.isEmpty(ids)) {
			StringBuilder sql = new StringBuilder("SELECT * FROM "+tableName+" WHERE ID in (");
			for (Iterator<Long> i = ids.iterator(); i.hasNext();) {
				sql.append(i.next());
				if(i.hasNext()) {
					sql.append(",");
				}
			}
			sql.append(")");
			return jdbcTemplate.query(sql.toString(),mapper);
		}
		return null;
	}
	
}
