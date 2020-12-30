package io.github.reinershir.auth.core.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.integrate.access.RoleAccess;
import io.github.reinershir.auth.core.model.RolePermission;

public class Appointor {

	RedisTemplate<String,String> redisTemplate;
	RoleAccess roleAccess;
	public Appointor(RedisTemplate<String,String> redisTemplate,RoleAccess roleAccess) {
		this.redisTemplate=redisTemplate;
		this.roleAccess=roleAccess;
	}
	/**
	 * @Title: grantTemporaryPermission
	 * @Description:   授予临时权限
	 * @author reinershir
	 * @date 2020年11月13日
	 * @param userId 用户ID标识
	 * @param permissionCodes 权限码
	 * @param expireSeconds 权限失效时间(单位为秒)
	 */
	public void grantTemporaryPermission(@Nonnull String userId,Set<String> permissionCodes,Long expireSeconds) {
		String temporaryPermissionKey = userId+AuthContract.TEMPORARY_PERMISSION_KEY;
		//为保证原子性，使用lua脚本执行
		StringBuilder script = new StringBuilder("redis.call('del',KEYS[1]) ");
		script.append("if redis.call('sadd',KEYS[1],");
		for (int i = 0; i < permissionCodes.size(); i++) {
			script.append("ARGV[");
			script.append((i+1));
			script.append("]");
			if((i+1)<permissionCodes.size()) {
				script.append(",");
			}
		}
		script.append(") > 0 then ");
		script.append("return redis.call('expire',KEYS[1],");
		script.append(expireSeconds);
		script.append(")  else return 0 end ");
		RedisScript<Long> redisScript = new DefaultRedisScript<>(script.toString(),Long.class); 
		redisTemplate.execute(redisScript, Collections.singletonList(temporaryPermissionKey), permissionCodes.toArray());
	}
	
	/**
	 * @Title: gratPermissionByUser
	 * @Description:   根据该用户绑定的角色授予临时权限
	 * @author reinershir
	 * @param userId 用户ID
	 * @param expireSeconds 权限失效时间(单位为秒)
	 * @date 2020年12月1日
	 * @param userId
	 */
	public void gratPermissionByUser(String userId,Long expireSeconds) {
		if(roleAccess!=null) {
			Set<Long> roleIds = roleAccess.getRoleByUser(userId);
			if(!CollectionUtils.isEmpty(roleIds)) {
				Set<String> permissionCodes = new HashSet<>();
				for (Long roleId : roleIds) {
					List<RolePermission> list = roleAccess.selectRolePermissionByRole(roleId);
					for (RolePermission rp : list) {
						if(rp!=null&&!StringUtils.isEmpty(rp.getPermissionCodes())) {
							if(rp.getPermissionCodes().indexOf(",")!=-1) {
								permissionCodes.addAll(Arrays.asList(rp.getPermissionCodes().split(",")));
							}else {
								permissionCodes.add(rp.getPermissionCodes());
							}
						}
					}
				}
				grantTemporaryPermission(userId,permissionCodes,expireSeconds);
			}
		}
	}
	
	/**
	 * @Title: removeAllTemporaryPermission
	 * @Description:   移除所有权限
	 * @author reinershir
	 * @date 2020年11月16日
	 * @param userId
	 * @return
	 */
	public boolean removeAllTemporaryPermission(@Nonnull String userId) {
		String temporaryPermissionKey = userId+AuthContract.TEMPORARY_PERMISSION_KEY;
		return redisTemplate.delete(temporaryPermissionKey);
	}
	
	/**
	 * @Title: removePermission
	 * @Description:   移除该用户的指定权限
	 * @author reinershir
	 * @date 2020年11月16日
	 * @param userId
	 * @param permissionCodes
	 * @return
	 */
	public long removePermission(@Nonnull String userId,Set<String> permissionCodes) {
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		return redisTemplate.opsForSet().remove(permissionRedisKey, permissionCodes.toArray());
	}
	
	/**
	 * @Title: grantPermission
	 * @Description:   永久覆盖授权，将会覆盖老的权限
	 * @author reinershir
	 * @date 2020年11月13日
	 */
	/*public void grantPermission(String userId,Set<String> permissionCodes) {
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		//为保证原子性，使用lua脚本执行
		StringBuilder script = new StringBuilder("if redis.call('del',KEYS[1]) > 0 then return redis.call('sadd',KEYS[1],");
		for (int i = 0; i < permissionCodes.size(); i++) {
			script.append("ARGV[");
			script.append((i+1));
			script.append("]");
			if((i+1)<permissionCodes.size()) {
				script.append(",");
			}
		}
		script.append(") else return 0 end ");
		RedisScript<Long> redisScript = new DefaultRedisScript<>(script.toString(),Long.class); 
		redisTemplate.execute(redisScript, Collections.singletonList(permissionRedisKey), permissionCodes.toArray());

	}*/
	
	/**
	 * @Title: addPermission
	 * @Description:   为用户添加永久权限，即在原有权限的基础上添加新的权限
	 * @author reinershir
	 * @date 2020年11月13日
	 * @param userId 用户ID
	 * @param permisionCodes 权限码
	 */
	/*public void addPermission(String userId,String ... permisionCodes) {
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		redisTemplate.opsForSet().add(permissionRedisKey, permisionCodes);
	}*/
	
	public Set<String> getPermissionsByUser(@Nonnull String userId){
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		return redisTemplate.opsForSet().members(permissionRedisKey);
	}
	
	public Set<String> getTemporaryPermissionByUser(String userId){
		String temporaryPermissionKey = userId+AuthContract.TEMPORARY_PERMISSION_KEY;
		return redisTemplate.opsForSet().members(temporaryPermissionKey);
	}
	public RoleAccess getRoleAccess() {
		return roleAccess;
	}
	
	
	
	
}
