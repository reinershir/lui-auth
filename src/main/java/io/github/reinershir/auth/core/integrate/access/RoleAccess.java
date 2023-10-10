package io.github.reinershir.auth.core.integrate.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import io.github.reinershir.auth.core.model.Role;
import io.github.reinershir.auth.core.model.RolePermission;
import io.github.reinershir.auth.core.model.RoleUser;

public class RoleAccess extends AbstractAccess<Role>{
	
	Logger logger = LoggerFactory.getLogger(getClass());

	String menuTableName;
	RedisTemplate<String,String> redisTemplate;
	RoleRowMapper  mapper = new RoleRowMapper();
	RolePermissionMapper permissionMapper = new RolePermissionMapper();
	RoleUserMapper roleUserMapper = new RoleUserMapper();
	
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
		return jdbcTemplate.query("SELECT * FROM "+tableName+"_PERMISSION WHERE ROLE_ID = ?",permissionMapper,roleId);
	}
	
	public List<RolePermission> selectRolePermissionByUser(String userId){
		List<Long> roleIds = getRoleIdByUser(userId);;
		return selectRolePermissionByList(roleIds);
		
	}
	
	public List<RolePermission> selectRolePermissionByList(List<Long> roleIds){
		if(!CollectionUtils.isEmpty(roleIds)) {
			StringBuilder sql = new StringBuilder("SELECT * FROM "+tableName+"_PERMISSION WHERE ROLE_ID in (");
			for (Iterator<Long> i = roleIds.iterator(); i.hasNext();) {
				sql.append(i.next());
				if(i.hasNext()) {
					sql.append(",");
				}
			}
			sql.append(")");
			return jdbcTemplate.query(sql.toString(),permissionMapper);
		}
		return null;
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
			role.setId(getIdByKeyholder(holder));
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
			
			
			if(!CollectionUtils.isEmpty(menuIds)) {
				StringBuilder sql = new StringBuilder("INSERT INTO "+tableName+"_PERMISSION(ROLE_ID,MENU_ID,PERMISSION_CODES) SELECT ?,?,");
				sql.append(menuTableName);
				sql.append(".PERMISSION_CODES FROM ");
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
	
	private int deleteRolePermission(Long roleId) {
		return jdbcTemplate.update("DELETE FROM "+tableName+"_PERMISSION WHERE ROLE_ID=?",roleId);
	}
	
	@Transactional
	public int deleteById(@Nonnull Long roleId) {
		if(roleId!=null) {
			//删除角色绑定的菜单
			deleteRolePermission(roleId);
			
			//删除用户所绑定的角色
			deleteRoleUserByRoleId(roleId);
			//删除角色
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
	@Transactional
	public boolean bindRoleForUser(String userId,List<Long> roleIds) {
		if(userId!=null&&!CollectionUtils.isEmpty(roleIds)) {

			StringBuilder sql = new StringBuilder("DELETE FROM "+tableName+"_USER WHERE USER_ID = ?");
			jdbcTemplate.update(sql.toString(),userId);
			
			sql = new StringBuilder("INSERT INTO "+tableName+"_USER(ROLE_ID,USER_ID) values(?,?)");
			 
			jdbcTemplate.batchUpdate(sql.toString(), new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					Long roleId = roleIds.get(i);
					ps.setLong(1, roleId);
					ps.setString(2, userId);
				}
				@Override
				public int getBatchSize() {
					return roleIds.size();
				}
			});
			

			return true;
		}
		return false;
	}
	
	private int deleteRoleUserByRoleId(Long roleId) {
		return jdbcTemplate.update("DELETE FROM "+tableName+"_USER WHERE ROLE_ID = ?",roleId);
	}
	
	/**
	 * @Title: getRoleUserByUserId
	 * @Description:   获取该用户绑定的角色ID对象信息
	 * @author reinershir
	 * @date 2021年3月2日
	 * @param userId
	 * @return  返回角色ID对象列表
	 */
	public List<RoleUser> getRoleUserByUserId(String userId){
		return jdbcTemplate.query("SELECT * FROM "+tableName+"_USER WHERE USER_ID=?",roleUserMapper,userId);
	}
	
	/**
	 * @Title: getRoleByUser
	 * @Description:   获取该用户绑定的角色ID
	 * @author reinershir
	 * @date 2021年3月2日
	 * @param userId
	 * @return  返回角色ID对象列表
	 */
	public List<Long> getRoleIdByUser(String userId){
		List<RoleUser> roleUsers = getRoleUserByUserId(userId);
		return getRoleIdsByRoleUsers(roleUsers);
	}
	
	public List<Role> selectRoleByUser(String userId){
		List<Long> roleIds = getRoleIdByUser(userId);
		return selectByList(roleIds);
	}
	
	/**
	 * @Title: selectUserIdByRole
	 * @Description:   根据角色ID获取该角色所绑定的用户ID
	 * @author reinershir
	 * @date 2021年3月2日
	 * @param roleId 角色ID
	 * @return 返回用户ID列表
	 */
	public List<String> selectUserIdByRole(Long roleId){
		List<RoleUser> roleUsers = jdbcTemplate.query("SELECT * FROM "+tableName+"_USER WHERE ROLE_ID=?",roleUserMapper,roleId);
		List<String> userIds = new ArrayList<>();
		if(!CollectionUtils.isEmpty(roleUsers)) {
			roleUsers.forEach((roleUser)->{
				userIds.add(roleUser.getUserId());
			});
		}
		return userIds;
	}
	
	/**
	 * @Title: getRoleIdsByRoleUsers
	 * @Description:   从角色用户关系对象中抽取角色ID并返回数组
	 * @author ReinerShir
	 * @date 2021年3月2日
	 * @param roleUsers
	 * @return 返回角色ID列表
	 */
	private List<Long> getRoleIdsByRoleUsers(List<RoleUser> roleUsers){
		List<Long> roleIds = new ArrayList<>();
		if(!CollectionUtils.isEmpty(roleUsers)) {
			roleUsers.forEach((roleUser)->{
				roleIds.add(roleUser.getRoleId());
			});
		}
		return roleIds;
	}
	
	public Role selectById(Long id) {
		return super.selectById(id, mapper);
	}
	
	public List<Role> selectByList(List<Long> ids){
		return super.selectByList(ids, mapper);
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
	
	public class RolePermissionMapper implements RowMapper<RolePermission> {
		@Override
		public RolePermission mapRow(ResultSet rs, int rowNum) throws SQLException {
			RolePermission r = new RolePermission();
			r.setId(rs.getLong("ID"));
			r.setMenuId(rs.getLong("MENU_ID"));
			r.setPermissionCodes(rs.getString("PERMISSION_CODES"));
			r.setRoleId(rs.getLong("ROLE_ID"));
			return r;
		}
	}
	
	public class RoleUserMapper implements RowMapper<RoleUser> {
		@Override
		public RoleUser mapRow(ResultSet rs, int rowNum) throws SQLException {
			RoleUser r = new RoleUser();
			r.setId(rs.getLong("ID"));
			r.setUserId(rs.getString("USER_ID"));
			r.setRoleId(rs.getLong("ROLE_ID"));
			return r;
		}
	}
}
