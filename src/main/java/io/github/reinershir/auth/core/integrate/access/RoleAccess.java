package io.github.reinershir.auth.core.integrate.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.model.Role;
import io.github.reinershir.auth.core.model.RolePermission;

public class RoleAccess extends AbstractAccess<Role>{
	
	Logger logger = LoggerFactory.getLogger(getClass());

	String menuTableName;
	RedisTemplate<String,String> redisTemplate;
	RoleRowMapper  mapper = new RoleRowMapper();
	
	public RoleAccess(JdbcTemplate jdbcTemplate,String tableName,String menuTableName,RedisTemplate<String,String> redisTemplate) {
		super(jdbcTemplate,tableName);
		this.menuTableName=menuTableName;
		this.redisTemplate=redisTemplate;
	}
	
	
	public List<Role> selectList(@Nonnull Integer page,@Nonnull Integer pageSize,@Nullable String roleName){
		return qureyList(page,pageSize,roleName,"ROLE_NAME",mapper);
	}
	
	public List<Role> selectList(@Nonnull Integer page,@Nonnull Integer pageSize){
		return qureyList(page,pageSize,null,"ROLE_NAME",mapper);
	}
	
	public Long selectCount(@Nullable String roleName) {
		return super.selectCount(roleName, "ROLE_NAME");
	}
	
	public List<RolePermission> selectRolePermissionByRole(Long roleId){
		return jdbcTemplate.query("SELECT * FROM "+tableName+"_PERMISSION WHERE ROLE_ID = ?",new RowMapper<RolePermission>() {
			@Override
			public RolePermission mapRow(ResultSet rs, int rowNum) throws SQLException {
				RolePermission r = new RolePermission();
				r.setId(rs.getLong("ID"));
				r.setMenuId(rs.getLong("MENU_ID"));
				r.setPermissionCodes(rs.getString("PERMISSION_CODES"));
				r.setRoleId(rs.getLong("ROLE_ID"));
				return r;
			}
		},roleId);
	}
	
	@Transactional
	public int insert(@Nonnull Role role,@Nullable List<RolePermission> rolePermissions) {
		if(role!=null) {
			int result = insertRole(role);
			
			if(result>0&&!org.springframework.util.CollectionUtils.isEmpty(rolePermissions)) {
				jdbcTemplate.batchUpdate("INSERT INTO "+tableName+"_PERMISSION(ROLE_ID,MENU_ID,PERMISSION_CODES) VALUES(?,?,?)", new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						RolePermission rp = rolePermissions.get(i);
						ps.setLong(1, rp.getRoleId());
						ps.setLong(2, rp.getMenuId());
						ps.setString(3, rp.getPermissionCodes());
					}
					@Override
					public int getBatchSize() {
						return rolePermissions.size();
					}
					
					 
				});
			}
			
			return result;
		}
		return -1;
	}
	
	private int insertRole(@Nonnull Role role) {
		if(role!=null) {
			KeyHolder holder = new GeneratedKeyHolder();
			String sql = "INSERT INTO "+tableName+"(ROLE_NAME,DESCRIPTION,CREATE_DATE) VALUES(?,?,?)";
			int result =  jdbcTemplate.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
					PreparedStatement ps = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
					ps.setString(1, role.getRoleName());
					ps.setString(2, role.getDescription());
					ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
					return ps;
				}
			},holder);
			role.setId(holder.getKey().longValue());
			return result;
		}
		return -1;
	}
	
	@Transactional
	public int insert(@Nonnull Role role,@Nullable ArrayList<Long> menuIds) {
		if(role!=null) {
			int result = insertRole(role);
			
			if(result>0&&!org.springframework.util.CollectionUtils.isEmpty(menuIds)) {
				StringBuilder sql = new StringBuilder("INSERT INTO "+tableName+"_PERMISSION(ROLE_ID,MENU_ID,PERMISSION_CODES) SELECT ?,?,");
				sql.append(menuTableName);
				sql.append(".ID FROM ");
				sql.append(menuTableName);
				sql.append(" WHERE ");
				sql.append(menuTableName);
				sql.append(".ID = ?");
				jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
					@Override
					public void setValues(PreparedStatement ps, int i) throws SQLException {
						Long menuId = menuIds.get(i);
						ps.setLong(1, role.getId());
						ps.setLong(2, menuId);
						ps.setLong(3, menuId);
					}
					@Override
					public int getBatchSize() {
						return menuIds.size();
					}
					
					 
				});
			}
			
			return result;
		}
		return -1;
	}
	
	@Transactional
	public int updateById(@Nonnull Role role,List<Long> menuIds) {
		if(role!=null&&role.getId()!=null) {
			int result = -1;
			result = jdbcTemplate.update("UPDATE "+tableName+" SET ROLE_NAME=?,DESCRIPTION=?,UPDATE_DATE=? WHERE ID = ?", new PreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps) throws SQLException {
					ps.setString(1, role.getRoleName());
					ps.setString(2, role.getDescription());
					ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
					ps.setLong(4, role.getId());
				}
			});
			
			deleteRolePermission(role.getId());
			
			StringBuilder sql = new StringBuilder("INSERT INTO "+tableName+"_PERMISSION(ROLE_ID,MENU_ID,PERMISSION_CODES) SELECT ?,?,");
			sql.append(menuTableName);
			sql.append(".ID FROM ");
			sql.append(menuTableName);
			sql.append(" WHERE ");
			sql.append(menuTableName);
			sql.append(".ID = ?");
			 
			jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Long menuId = menuIds.get(i);
					ps.setLong(1, role.getId());
					ps.setLong(2, menuId);
					ps.setLong(3, menuId);
				}
				@Override
				public int getBatchSize() {
					return menuIds.size();
				}
			});
			return result;
		}
		return -1;
	}
	
	private int deleteRolePermission(Long roleId) {
		return jdbcTemplate.update("DELETE FROM "+tableName+"_PERMISSION WHERE ROLE_ID=?",roleId);
	}
	
	@Transactional
	public int deleteById(@Nonnull Long roleId) {
		if(roleId!=null) {
			deleteRolePermission(roleId);
			return super.deleteById(roleId);
		}
		return -1;
	}
	
	/**
	 * @Title: bindRoleForUser
	 * @Description:  为用户绑定角色，会覆盖老的绑定关系
	 * @author reinershir
	 * @date 2020年12月1日
	 * @param userId 用户ID
	 * @param roleIds 角色ID
	 * @return  true=success,false=faild
	 */
	public boolean bindRoleForUser(String userId,Set<Long> roleIds) {
		if(userId!=null&&!CollectionUtils.isEmpty(roleIds)) {
			String key = AuthContract.USER_ROLE_BIND_KEY+userId;
			//为保证原子性，使用lua脚本执行
			StringBuilder script = new StringBuilder("if redis.call('del',KEYS[1]) >= 0 then return redis.call('sadd',KEYS[1],");
			for (int i = 0; i < roleIds.size(); i++) {
				script.append("ARGV[");
				script.append((i+1));
				script.append("]");
				if((i+1)<roleIds.size()) {
					script.append(",");
				}
			}
			script.append(") else return 0 end ");
			RedisScript<Long> redisScript = new DefaultRedisScript<>(script.toString(),Long.class); 
			Set<String> roleIdSet = new HashSet<>();
			for (Long id : roleIds) {
				if(id!=null) {
					roleIdSet.add(id+"");
				}
			}
			redisTemplate.execute(redisScript, Collections.singletonList(key), roleIdSet.toArray());

			return true;
		}
		return false;
	}
	
	/**
	 * @Title: getRoleByUser
	 * @Description:   获取该用户绑定的角色ID
	 * @author reinershir
	 * @date 2020年12月1日
	 * @param userId
	 * @return  返回角色ID列表
	 */
	public Set<Long> getRoleIdByUser(String userId){
		String key = AuthContract.USER_ROLE_BIND_KEY+userId;
		Set<String> roleIds = redisTemplate.opsForSet().members(key);
		Set<Long> ids = new HashSet<>();
		if(!CollectionUtils.isEmpty(roleIds)) {
			for (String id : roleIds) {
				if(!StringUtils.isEmpty(id)) {
					try {
						ids.add(Long.parseLong(id));
					}catch (Exception e) {
						logger.error("Conversion failed : {}",id);
					}
				}
			}
		}
		return ids;
	}
	
	public List<Role> getRoleByUser(String userId){
		Set<Long> ids = getRoleIdByUser(userId);
		return selectByIds(ids);
	}
	
	public Role selectById(Long id) {
		return super.selectById(id, mapper);
	}
	
	public List<Role> selectByIds(Collection<Long> ids){
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
	
	public class RoleRowMapper implements  RowMapper<Role>{

		@Override
		public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
			Role role = new Role();
			role.setId(rs.getLong("ID"));
			role.setCreateDate(rs.getDate("CREATE_DATE"));
			role.setDescription(rs.getString("DESCRIPTION"));
			role.setRoleName(rs.getString("ROLE_NAME"));
			role.setUpdateDate(rs.getDate("UPDATE_DATE"));
			return role;
		}
		
	}
}
