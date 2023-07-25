package io.github.reinershir.auth.core.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;

import io.github.reinershir.auth.config.AuthorizationProperty;
import io.github.reinershir.auth.config.PermissionScanner;
import io.github.reinershir.auth.contract.AuthContract;
import io.github.reinershir.auth.core.integrate.access.MenuAccess;
import io.github.reinershir.auth.core.integrate.access.RoleAccess;
import io.github.reinershir.auth.core.model.Menu;
import io.github.reinershir.auth.core.model.RolePermission;
import io.github.reinershir.auth.entity.TokenInfo;
import io.github.reinershir.auth.utils.DESUtil;
import io.github.reinershir.auth.utils.JacksonUtil;
import jakarta.servlet.http.HttpServletRequest;

public class AuthorizeManager {

	AuthorizationProperty property;
	RedisTemplate<String,String> redisTemplate;
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	PermissionScanner scanner;
	Appointor appointor;
	MenuAccess menuAccess;
	Cache<String,TokenInfo> cache = null;
	
	public AuthorizeManager(AuthorizationProperty property,RedisTemplate<String,String> redisTemplate,PermissionScanner scanner,Appointor appointor,MenuAccess menuAccess) {
		this.redisTemplate=redisTemplate;
		this.property=property;
		Long expireTime = property.getAuthrizationConfig()!=null?property.getAuthrizationConfig().getTokenExpireTime():3600l;
		cache = CacheBuilder.newBuilder().expireAfterAccess(expireTime, TimeUnit.SECONDS).maximumSize(500).build();
		this.scanner=scanner;
		this.appointor=appointor;
		this.menuAccess=menuAccess;
	}
	
	/**
	 * @Title: authentication
	 * @Description:   验证该用户是否拥有指定权限
	 * @author reinershir
	 * @date 2020年11月13日
	 * @param token 
	 * @param permissionCode 要验证的权限码
	 * @return
	 */
	public int authentication(String token,String permissionCode) {
		int validateResult = validateTokenAndRenewal(token);
		if(validateResult==AuthContract.AUTHORIZATION_STATUS_SUCCESS) {
			String generateToken = token.split("_")[1];
			String requiredToken = token.split("_")[0];
			//先从缓存中获取
			TokenInfo tokenInfo=null;
			try {
				tokenInfo = cache.get(generateToken,()->{
					String tokenInfoJson = DESUtil.decryption(requiredToken, property.getAuthrizationConfig().getTokenSalt());
					TokenInfo info = JacksonUtil.readValue(tokenInfoJson, TokenInfo.class);
					//缓存一下token信息
					cache.put(generateToken, info);
					return info;
				});
			} catch (ExecutionException | InvalidCacheLoadException e1) {
				return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
			}
			
			String permissionRedisKey = tokenInfo.getUserId()+AuthContract.TEMPORARY_PERMISSION_KEY;
			//存在权限通过请求
			if(redisTemplate.opsForSet().isMember(permissionRedisKey, permissionCode)) {
				//续期权限数据
				redisTemplate.expire(permissionRedisKey, property.getAuthrizationConfig().getTokenExpireTime(),TimeUnit.SECONDS);
				return AuthContract.AUTHORIZATION_STATUS_SUCCESS;
			}
			//否则没有权限
			return AuthContract.AUTHORIZATION_STATUS_NO_PERMISSION;
		}
		return validateResult;
		
	}
	
	/**
	 * @Title: validateTokenAndRenewal
	 * @Description:   验证token是否有效并续期
	 * @author reinershir
	 * @date 2020年12月4日
	 * @param token 未解密前的token
	 * @return 返回验证结果，参考常量：@see io.github.reinershir.auth.contract.AuthContract
	 */
	public int validateTokenAndRenewal(String token) {
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
			logger.warn("expire token : {}",token);
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		String realToken = redisTemplate.opsForValue().get(generateToken);
		//任意一个token为空都说明token不正确
		if(!isNotEmpty(realToken,generateToken,requiredToken)||!requiredToken.equals(realToken)) {
			logger.error("Illegal token : {}",token);
			return AuthContract.AUTHORIZATION_STATUS_ILLEGAL;
		}
		//续期token
		redisTemplate.expire(generateToken, property.getAuthrizationConfig().getTokenExpireTime(),TimeUnit.SECONDS);
		return AuthContract.AUTHORIZATION_STATUS_SUCCESS;
	}
	
	
	
	
	/**
	 * @Title: generateToken
	 * @Description:   生成并保存token
	 * @author reinershir
	 * @date 2020年11月11日
	 * @param userId 用户唯一标识
	 * @param userType 用户类型,可为空
	 * @return token
	 * @throws Exception
	 */
	public String generateToken(@Nonnull String userId,@Nullable Integer userType) throws Exception {
		return saveTokenToRedis(new TokenInfo(userType,userId,UUID.randomUUID().toString(),null));
	}
	
	/**
	 * @Description:   生成并保存token
	 * @param userId 用户唯一标识
	 * @param userType 用户类型,可为空
	 * @param ip 用户的IP，绑定IP模式下必填
	 * @throws Exception
	 * @author reinershir
	 * @date 2023年6月15日
	 * @return String
	 */
	public String generateToken(@Nonnull String userId,@Nullable Integer userType,String ip) throws Exception {
		return saveTokenToRedis(new TokenInfo(userType,userId,UUID.randomUUID().toString(),ip,null));
	}
	
	/**
	 * @Title: generateToken
	 * @Description:   生成并保存Token到redis，可附带用户信息到Token中
	 * @author reiner_shir
	 * @date 2020年12月30日
	 * @param userId 用户ID
	 * @param userType 用户类型
	 * @param userInfo 要保存在Token中的用户信息(注意Long类型会在Json序列化时降为Integer类型)
	 * @return 生成的Token
	 * @throws Exception
	 */
	public  String generateToken(@Nonnull String userId,@Nullable Integer userType,@Nullable Map<String,Object> userInfo) throws Exception {
		return saveTokenToRedis(new TokenInfo(userType,userId,UUID.randomUUID().toString(),userInfo));
	}
	
	/**
	 * @Title: generateToken
	 * @Description:   生成并保存Token到redis，可附带用户信息到Token中
	 * @author xh
	 * @date 2020年12月30日
	 * @param userId 用户ID
	 * @param userType 用户类型
	 * @param ip 用户的IP，绑定IP模式下必填
	 * @param userInfo 要保存在Token中的用户信息(注意Long类型会在Json序列化时降为Integer类型)
	 * @return 生成的Token
	 * @throws Exception
	 */
	public  String generateToken(@Nonnull String userId,@Nullable Integer userType,String ip,@Nullable Map<String,Object> userInfo) throws Exception {
		return saveTokenToRedis(new TokenInfo(userType,userId,UUID.randomUUID().toString(),ip,userInfo));
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
		return saveToken(tokeninfo,property.getAuthrizationConfig().getTokenExpireTime());
	}
	
	private String saveToken(TokenInfo tokeninfo,Long expireTime) throws Exception {
		String userId = tokeninfo.getUserId();
		//将随机生成的TOKEN使用AES加密
		String realToken = DESUtil.encryption(JacksonUtil.toJSon(tokeninfo),property.getAuthrizationConfig().getTokenSalt());
		//生成根据用户ID固定生成的TOKEN
		String generateToken =  DESUtil.encryption(userId+"_"+tokeninfo.getUserType(),property.getAuthrizationConfig().getTokenSalt());
		//两个token的组合使用，前半段随机生成，后半段固定加密
		String returnToken = realToken+"_"+generateToken;
		//根据该用户绑定的角色授权
		String administratorId = property.getAuthrizationConfig().getAdministratorId();
		//判断是否是超级管理员
		if(!StringUtils.isEmpty(administratorId)&&administratorId.equals(userId)) {
			appointor.grantTemporaryPermission(userId, getAllPermissionCodes(), property.getAuthrizationConfig().getTokenExpireTime());
		}else {
			appointor.gratPermissionByUser(userId, property.getAuthrizationConfig().getTokenExpireTime());
		}
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
	 * @throws ExecutionException 
	 * @Description: 解密token，获取token中的信息，token中包含用户类型、用户ID等信息
	 * @param: @param token
	 * @param: @return      
	 * @return: TokenInfo      
	 * @throws
	 */
	public TokenInfo getTokenInfo(HttpServletRequest request) {
		String token = request.getHeader(property.getAuthrizationConfig().getTokenHeaderName());
		if(!StringUtils.isEmpty(token)&&token.indexOf("_")!=-1) {
			String generateToken = token.split("_")[1];
			String requiredToken = token.split("_")[0];
			TokenInfo tokenInfo = null;
			try {
				tokenInfo = cache.get(generateToken,()->{
					String tokenInfoJson = DESUtil.decryption(requiredToken, property.getAuthrizationConfig().getTokenSalt());
					TokenInfo info = JacksonUtil.readValue(tokenInfoJson, TokenInfo.class);
					//缓存一下token信息
					cache.put(generateToken, info);
					return info;
				});
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			return tokenInfo;
		}
		return null;
	}
	
	/**
	 * 
	 * @Title: logout
	 * @Description:   获取HTTP头中token信息并登出
	 * @author Reiner_Shir
	 * @date 2021年6月17日
	 * @param request
	 */
	public void logout(HttpServletRequest request) {
		String token =  request.getHeader(property.getAuthrizationConfig().getTokenHeaderName());
		if(!StringUtils.isEmpty(token)) {
			String userInfoToken = token.split("_")[1];
			if(!StringUtils.isEmpty(userInfoToken)) {
				redisTemplate.delete(userInfoToken);
			}
			TokenInfo tokenInfo = getTokenInfo(request);
			if(tokenInfo!=null) {
				this.appointor.removeAllTemporaryPermission(tokenInfo.getUserId().toString());
			}
		}
		
	}
	
	/**
	 * @Title: logout
	 * @Description:  根据用户ID登出 
	 * @author Reiner_Shir
	 * @date 2021年6月17日
	 * @param userId
	 * @throws Exception
	 */
	public void logout(String userId) throws Exception {
		String generateToken =  DESUtil.encryption(userId,property.getAuthrizationConfig().getTokenSalt());
		redisTemplate.delete(generateToken);
		this.appointor.removeAllTemporaryPermission(userId);
	}
	
	/**
	 * @Title: getMenusByUser
	 * @Description:  获取该用户绑定的角色所拥有的菜单，超级管理员将拥有所有菜单权限
	 * @author reinershir
	 * @date 2021年1月25日
	 * @param userId 用户ID
	 * @return 菜单列表
	 */
	public List<Menu> getMenusByUser(@Nonnull String userId){
		if(!StringUtils.isEmpty(userId)&&userId.equals(property.getAuthrizationConfig().getAdministratorId())) {
			return menuAccess.qureyList(null);
		}else {
			List<RolePermission> rolePermissions = getRoleAccess().selectRolePermissionByUser(userId);
			if(!CollectionUtils.isEmpty(rolePermissions)) {
				List<Long> ids = new ArrayList<>();
				rolePermissions.forEach((p)->{
					ids.add(p.getMenuId());
				});
				return menuAccess.selectByList(ids);
			}
		}
		
		return null;
	}
	
	/**
	 * @Title: getAllPermissionCodes
	 * @Description:   获取本服务所有权限码
	 * @author reinershir
	 * @date 2020年11月13日
	 * @return
	 */
	public Set<String> getAllPermissionCodes(){
		return scanner.getPermissionCodes();
	}
	
	public MenuAccess getMenuAccess() {
		return this.menuAccess;
	}
	
	public RoleAccess getRoleAccess() {
		return appointor.getRoleAccess();
	}
	
	public Appointor getAppointor() {
		return appointor;
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
