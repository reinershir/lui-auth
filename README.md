# lui-auth

一个依赖于spring boot简单的权限验证工具，集成角色、菜单、权限功能，无需下载工程，无复杂配置，只需依赖jar并简单配置即可使用。

特点如下：<br/>
  1、配置简单	<br/>
  2、集成了菜单、角色、权限管理	<br/>
  3、支持同一账号只能一人登陆	<br/>
  4、使用注解标记权限，减少代码入侵	<br/>
  5、使用redis存储权限信息	<br/>
  6、菜单管理支持无限层级树形结构，使用左右值树形结构(modified preorder tree traversal)存储，查询效率非常快
  
  
# 开始使用

## 添加依赖
```xml
<dependency>
	<groupId>io.github.reinershir.auth</groupId>
	<artifactId>lui-auth</artifactId>
	<version>0.0.22-RELEASE</version>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```

## 启动类添加注解开关
在你的项目启动类添加@EnableAuthentication注解开关
```java
@SpringBootApplication
@EnableAuthentication
public class Application {
	
	public static void main(String[] args) {
		Application.run(Application.class, args);
	}
}
```

## 配置redis连接信息和token加密密钥

```yml
spring:
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
    password: hongzhu123
    # 连接超时时间（毫秒）
    timeout: 3000
	
lui-auth:
  authrizationConfig: 
    administratorId: 1  #超级管理员用户ID，防止角色被全删无法登陆的情况，该用户ID一登陆即拥有所有权限
    tokenSalt: yorTokenSalt   #生成token的盐
	tokenExpireTime: 1800   #token失效时间，默认30分钟,单位为秒
  intergrateConfig: 
    enable: true   #使用集成的角色、菜单管理功能，将会自动生成三张表，提供增删改查接口
```



## 配置拦截器

以下为spring boot配置方式：
```java
@EnableWebMvc
public class WebMvcConfig  implements WebMvcConfigurer {

	@Autowired(required=false)
	AuthenticationInterceptor authenticationInterceptor;
	
	/**
	 * 添加拦截器
	 */
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
		if(authenticationInterceptor!=null){
			registry.addInterceptor(authenticationInterceptor);
		}
    }
```

## 给需要鉴权的接口添加注解标记

以controller为例：

```java
@RequestMapping("test")
@RestController
@PermissionMapping(value = "TEST")
public class ExampController {

	@Permission(name = "测试redis",value = OptionType.LIST)
	@GetMapping("testRedis")
	public Object test(String param) {
		return "";
	}
}
```

拦截器通过接口标记的权限码验证，如@PermissionMapping的value=TEST,下面的test接口@Permission配置的是OptionType.LIST，那么权限码就是： TEST:LIST

权限码可自定义 : `@Permission(name = "测试接口",value = OptionType.CUSTOM,customPermissionCode = "MYCUSTOM")`


## 最后一步，生成token

在登陆接口验证完账号密码后调用以下接口生成token返回到前端：

```java

@RestController
@RequestMapping("user")
public class LoginController {

	@Autowired
	AuthorizeManager authorizeManager;
	
	@PostMapping("login")
	public Object login(@RequestBody LoginInfoDTO loginInfo) {
		//登陆验证完成后
		String userId = "你的用户ID唯一标识";
		Sint userType = 1; //用户类型标记
		String token = authorizeManager.generateToken(userId, userType); //如果ID= 配置的administratorId，会拥有所有权限
		return token;
	}
}
```




前端传token时需要在http header里添加：  Access-Token: 登陆接口返回的token  ,header name是可配置的，默认Access-Token



# 其它说明

`intergrateConfig.enable=true` 开启时会自动生成3张表，分别为角色表、菜单表、角色权限表，3张表提供增删改查接口  

*跳过权限验证：*  
1、控制器和方法上都不加注解
2、控制器类上加了注解，使用注解跳过单个接口,示例： `@Permission(OptionType.SKIP)` 

# 角色、菜单集成功能使用示例

### 角色表增删改查接口示例

```java
@RequestMapping("role")
@RestController
@PermissionMapping(value="ROLE")
public class RoleController {
	
	
	RoleAccess roleAccess;
	@Autowired
	public RoleController(AuthorizeManager authorizeManager) {
		this.roleAccess=authorizeManager.getRoleAccess();
	}

	@Permission(name = "角色列表",value = OptionType.LIST)
	@GetMapping("list")
	public ResultDTO<PageBean<Role>> list(@Validated PageReqDTO reqDTO){
		List<io.github.reinershir.auth.core.model.Role> list = roleAccess.selectList(reqDTO.getPage(), reqDTO.getPageSize());
		Long count = roleAccess.selectCount(null);
		return ResponseUtil.generateSuccessDTO(new PageBean<>(reqDTO.getPage(),reqDTO.getPageSize(),count,list));
	}
	
	@Permission(name = "添加角色",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addRole(@Validated @RequestBody RoleDTO dto){
		if(roleAccess.insert(dto,dto.getMenuIds())>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("添加失败！");
	}
	
	@Permission(name = "修改角色信息",value = OptionType.UPDATE)
	@PatchMapping
	public ResultDTO<Object> updateUser(@Validated(value = ValidateGroups.UpdateGroup.class) @RequestBody RoleDTO roleDTO){
		if(roleAccess.updateById(roleDTO, roleDTO.getMenuIds())>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("修改失败！");
	}
	
	@Permission(name = "删除角色",value = OptionType.DELETE)
	@DeleteMapping("/{id}")
	public ResultDTO<Object> delete(@PathVariable("id") Long id){
		if(roleAccess.deleteById(id)>0) {
			return ResponseUtil.generateSuccessDTO("删除成功！");
		}
		return ResponseUtil.generateFaileDTO("修改失败！");
	}
	
	@Permission(name = "查询角色所绑定的菜单权限",value = OptionType.CUSTOM,customPermissionCode = "ROLE_PERMISSION")
	@GetMapping("/{roleId}/rolePermissions")
	public ResultDTO<List<RolePermission>> getRolePermissionsById(@PathVariable("roleId") Long roleId){
		return ResponseUtil.generateSuccessDTO(roleAccess.selectRolePermissionByRole(roleId));
	}
}

```

### 菜单表接口使用示例

```java
@RequestMapping("Menu")
@RestController
@PermissionMapping(value="MENU")
public class MenuController {
	
	
	MenuAccess MenuAccess;
	@Autowired
	public MenuController(AuthorizeManager authorizeManager) {
		this.MenuAccess=authorizeManager.getMenuAccess();
	}

	@Permission(name = "菜单列表",value = OptionType.LIST)
	@GetMapping("list")
	public ResultDTO<List<Menu>> list(@RequestParam(value="parentId",required = false) Long parentId){
		return ResponseUtil.generateSuccessDTO(MenuAccess.qureyList(parentId));
	}
	
	@Permission(name = "添加菜单",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addMenu(@Validated @RequestBody MenuVO menu,@RequestParam(value="parentId",required = false) Long parentId){
		if(MenuAccess.insertMenu(menu,parentId)>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("添加失败！");
	}
	
	@Permission(name = "修改菜单信息",value = OptionType.UPDATE)
	@PatchMapping
	public ResultDTO<Object> updateMenu( @RequestBody MenuVO MenuDTO){
		if(MenuAccess.updateById(MenuDTO)>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("修改失败！");
	}
	
	@Permission(name = "删除菜单",value = OptionType.DELETE)
	@DeleteMapping("/{id}")
	public ResultDTO<Object> delete(@PathVariable("id") Long id){
		if(MenuAccess.deleteById(id)>0) {
			return ResponseUtil.generateSuccessDTO("删除成功！");
		}
		return ResponseUtil.generateFaileDTO("修改失败！");
	}
}

```

### 为用户绑定角色示例

```java
@Autowired
AuthorizeManager authorizeManager;

...
//为用户绑定角色
if(!CollectionUtils.isEmpty(roleIds)) {
	authorizeManager.getRoleAccess().bindRoleForUser(userId, roleIds);
}


//获取用户绑定的角色：
authorizeManager.getRoleAccess().getRoleByUser(userId);
```

初始版本还很渣，后续会渐渐的增加功能并优化，逐渐思考新的鉴权方式

# TODO LIST

1、独立为一个单独的鉴权服务，支持微服务注册中心、对称密钥等调用方式	<br/>

2、IP白黑名称	<br/>

3、用户限流	<br/>

4、数据权限（构想中...）	<br/>

5、支持redisson

6、集成的增删改暂时只支持oracle和mysql








