package io.github.reinershir.auth.core.support;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;

import io.github.reinershir.auth.config.AuthorizationProperty;
import io.github.reinershir.auth.config.PermissionScanner;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.entity.Menu;
import io.github.reinershir.auth.entity.TokenInfo;
import io.github.reinershir.auth.utils.DESUtil;
import io.github.reinershir.auth.utils.JacksonUtil;

public class AuthorizeManager {

	AuthorizationProperty property;
	StringRedisTemplate redisTemplate;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	PermissionScanner scanner;
	
	Cache<String,TokenInfo> cache = null;
	
	public AuthorizeManager(AuthorizationProperty property,StringRedisTemplate redisTemplate,PermissionScanner scanner) {
		this.redisTemplate=redisTemplate;
		this.property=property;
		cache = CacheBuilder.newBuilder().expireAfterAccess(property.getTokenExpireTime(), TimeUnit.SECONDS).maximumSize(500).build();
		this.scanner=scanner;
	}
	
	/**
	 * @Title: authentication
	 * @Description:   验证该用户是否拥有指定权限
	 * @author xh
	 * @date 2020年11月13日
	 * @param token 
	 * @param permissionCode 要验证的权限码
	 * @return
	 */
	public int authentication(String token,String permissionCode) {
		if(StringUtils.isEmpty(token)) {
			logger.error("Authentication Token Is Null!");
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		if(token.indexOf("_")==-1) {
			logger.error("Illegal token! Incorrect format token.");
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		String generateToken = token.split("_")[1];
		String requiredToken = token.split("_")[0];
		Long expire = redisTemplate.opsForValue().getOperations().getExpire(generateToken);
		//判断是否存在或过期
		if(expire<1) {
			logger.warn("Illegal token : {}",token);
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		String realToken = redisTemplate.opsForValue().get(generateToken);
		//任意一个token为空都说明token不正确
		if(!isNotEmpty(realToken,generateToken,requiredToken)||!requiredToken.equals(realToken)) {
			logger.error("Illegal token : {}",token);
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		//续期token
		redisTemplate.expire(generateToken, property.getTokenExpireTime(),TimeUnit.SECONDS);
		
		//先从缓存中获取
		TokenInfo tokenInfo=null;
		try {
			tokenInfo = cache.get(generateToken,()->{
				String tokenInfoJson = DESUtil.decryption(requiredToken, property.getTokenSalt());
				TokenInfo info = JacksonUtil.readValue(tokenInfoJson, TokenInfo.class);
				//缓存一下token信息
				cache.put(generateToken, info);
				return info;
			});
		} catch (ExecutionException | InvalidCacheLoadException e1) {
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		
		String permissionRedisKey = tokenInfo.getUserId()+AuthContract.PERMISSION_CODE_KEY;
		//存在权限通过请求
		if(redisTemplate.opsForSet().isMember(permissionRedisKey, permissionCode)) {
			return AuthContract.AUTHORIZATION_STATUS_SUCCESS;
		}
		
		//看看是否有临时权限
		String temporaryPermissionKey = tokenInfo.getUserId()+AuthContract.TEMPORARY_PERMISSION_KEY;
		//判断是否存在或过期
		if(redisTemplate.opsForValue().getOperations().getExpire(temporaryPermissionKey)<1) {
			logger.warn("No permission. user id:{},user type:{}",tokenInfo.getUserId(),tokenInfo.getUserType());
			return AuthContract.AUTHORIZATION_STATUS_NO_PERMISSION;
		}
		
		//存在权限通过请求
		if(redisTemplate.opsForSet().isMember(temporaryPermissionKey, permissionCode)) {
			return AuthContract.AUTHORIZATION_STATUS_SUCCESS;
		}
		
		return AuthContract.AUTHORIZATION_STATUS_NO_PERMISSION;
	}
	
	/**
	 * @Title: grantTemporaryPermission
	 * @Description:   授予临时权限,不会移除未失效的临时权限
	 * @author xh
	 * @date 2020年11月13日
	 * @param userId 用户ID标识
	 * @param permissionCodes 权限码
	 * @param expireSeconds 权限失效时间(单位为秒)
	 */
	public void grantTemporaryPermission(@Nonnull String userId,Set<String> permissionCodes,Long expireSeconds) {
		String temporaryPermissionKey = userId+AuthContract.TEMPORARY_PERMISSION_KEY;
		//为保证原子性，使用lua脚本执行
		StringBuilder script = new StringBuilder("if redis.call('sadd',KEYS[1],");
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
		script.append(") else return 0 end ");
		RedisScript<Long> redisScript = new DefaultRedisScript<>(script.toString(),Long.class); 
		redisTemplate.execute(redisScript, Collections.singletonList(temporaryPermissionKey), permissionCodes.toArray());
	}
	
	/**
	 * @Title: removeTemporaryPermission
	 * @Description:   移除单个临时权限
	 * @author xh
	 * @date 2020年11月16日
	 * @param userId
	 * @param permissionCodes
	 * @return
	 */
	public long removeTemporaryPermission(@Nonnull String userId,Set<String> permissionCodes) {
		String temporaryPermissionKey = userId+AuthContract.TEMPORARY_PERMISSION_KEY;
		return redisTemplate.opsForSet().remove(temporaryPermissionKey, permissionCodes.toArray());
	}
	
	/**
	 * @Title: removeAllTemporaryPermission
	 * @Description:   移除所有临时权限
	 * @author xh
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
	 * @Description:   移除该用户的永久权限
	 * @author xh
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
	 * @Title: removeAllPermission
	 * @Description:   移除该用户的所有永久权限
	 * @author xh
	 * @date 2020年11月16日
	 * @param userId
	 * @return
	 */
	public boolean removeAllPermission(@Nonnull String userId) {
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		return redisTemplate.delete(permissionRedisKey);
	}
	
	/**
	 * @Title: grantPermission
	 * @Description:   永久覆盖授权，将会覆盖老的权限
	 * @author xh
	 * @date 2020年11月13日
	 */
	public void grantPermission(String userId,Set<String> permissionCodes) {
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

	}
	
	/**
	 * @Title: addPermission
	 * @Description:   为用户添加永久权限，即在原有权限的基础上添加新的权限
	 * @author xh
	 * @date 2020年11月13日
	 * @param userId 用户ID
	 * @param permisionCodes 权限码
	 */
	public void addPermission(String userId,String ... permisionCodes) {
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		redisTemplate.opsForSet().add(permissionRedisKey, permisionCodes);
	}
	
	public Set<String> getPermissionsByUser(@Nonnull String userId){
		String permissionRedisKey = userId+AuthContract.PERMISSION_CODE_KEY;
		return redisTemplate.opsForSet().members(permissionRedisKey);
	}
	
	public Set<String> getTemporaryPermissionByUser(String userId){
		String temporaryPermissionKey = userId+AuthContract.TEMPORARY_PERMISSION_KEY;
		return redisTemplate.opsForSet().members(temporaryPermissionKey);
	}
	
	
	/**
	 * @Title: generateToken
	 * @Description:   生成并保存token
	 * @author xh
	 * @date 2020年11月11日
	 * @param userId 用户唯一标识
	 * @param userType 用户类型,可为空
	 * @return token
	 * @throws Exception
	 */
	public String generateToken(String userId,@Nullable Integer userType) throws Exception {
		return saveTokenToRedis(new TokenInfo(userType,userId,UUID.randomUUID().toString()));
	}

	/**
	 * @throws Exception 
	 * @Description: 生成并保存token到redis中
	 * @param: @param tokeninfo 要返回给前台的的token信息
	 * @param: @param user 要保存在redis中的用户信息
	 * @return: dealToken 将tokenInfo加密后的字符串      
	 * @throws
	 */
	private String saveTokenToRedis(TokenInfo tokeninfo) throws Exception {
		return saveToken(tokeninfo,property.getTokenExpireTime());
	}
	
	private String saveToken(TokenInfo tokeninfo,Long expireTime) throws Exception {
		//将最后生成的TOKEN使用AES加密
		String realToken = DESUtil.encryption(JacksonUtil.toJSon(tokeninfo),property.getTokenSalt());
		String generateToken =  DESUtil.encryption(tokeninfo.getUserId(),property.getTokenSalt());
		//两个token的组合，前半段随机生成，后半段固定加密
		String returnToken = realToken+"_"+generateToken;
		//为保证原子性，使用lua脚本执行
		String script =
                "if redis.call('set',KEYS[1],ARGV[1]) then" +
                        "   return redis.call('expire',KEYS[1],"+expireTime+") " +
                        "else" +
                        "   return 0 " +
                        "end";
		RedisScript<Long> redisScript = new DefaultRedisScript<>(script,Long.class); 
		redisTemplate.execute(redisScript,Collections.singletonList(generateToken),realToken);
		return returnToken;
	}
	
	/**
	 * @Description: 解密token，获取token中的信息，token中包含用户类型、用户ID等信息
	 * @param: @param token
	 * @param: @return      
	 * @return: TokenInfo      
	 * @throws
	 */
	public TokenInfo getTokenInfo(HttpServletRequest request) {
		String token = request.getHeader(property.getTokenHeaderName()).split("_")[0];
		String tokenInfoJson = DESUtil.decryption(token,property.getTokenSalt());
		return !StringUtils.isEmpty(tokenInfoJson)?JacksonUtil.readValue(tokenInfoJson, TokenInfo.class):null;
	}
	
	
	/**
	 * @Title: getAllPermissionCodes
	 * @Description:   获取本服务所有权限码
	 * @author xh
	 * @date 2020年11月13日
	 * @return
	 */
	public Set<String> getAllPermissionCodes(){
		return scanner.getPermissionCodes();
	}
	
	/**
	 * @Title: getPermissionMenu
	 * @Description:   获取本服务所有菜单数据
	 * @author xh
	 * @date 2020年11月13日
	 * @return
	 */
	public List<Menu> getPermissionMenu(){
		return scanner.getMenus();
	}
	
	private boolean isNotEmpty(String ...param) {
		for (String string : param) {
			if(StringUtils.isEmpty(string)) {
				return false;
			}
		}
		return true;
		
	}
}
