# lui-auth

<div align="center">
  <p>
  </p>

**简体中文** |[**English**](README.md) | [**日本語**](README.jp.md)

</div>

一个依赖于spring boot简单的权限验证工具，集成角色、菜单、权限功能，无需下载工程，无复杂配置，只需依赖jar并简单配置即可使用，当你不想使用任何脚手架和复杂依赖时它很有用。

特点如下：`<br/>`
  1、配置简单	`<br/>`
  2、集成了菜单、角色、权限管理	`<br/>`
  3、支持同一账号只能一人登陆	`<br/>`
  4、使用注解标记权限，减少代码入侵	`<br/>`
  5、使用redis存储权限信息	`<br/>`
  6、菜单管理支持无限层级树形结构，使用左右值树形结构(modified preorder tree traversal)存储，查询效率非常快

#### 前置环境

* Spring Boot 2.0 +
* Redis 5.0 +
* spring-boot-starter-data-redis 依赖
* JDK 1.8 +
* MYSQL 5.7+ OR ORACLE

#### Examp

简单示例地址：https://github.com/reinershir/lui-auth-examp

# 开始使用

## 添加依赖

```xml
<dependency>
	<groupId>io.github.reinershir.auth</groupId>
	<artifactId>lui-auth</artifactId>
	<version>1.2.4-RELEASE</version>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```

如果使用 spring boot 3.0 + :

```xml
<dependency>
	<groupId>io.github.reinershir.auth</groupId>
	<artifactId>lui-auth</artifactId>
	<version>2.0.1</version>
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
    password: pwd123
    # 连接超时时间（毫秒）
    timeout: 3000

lui-auth:
  authrizationConfig: 
    administratorId: 1  #超级管理员用户ID，防止角色被全删无法登陆的情况，该用户ID一登陆即拥有所有权限
    tokenSalt: yorTokenSalt   #生成token的盐
    tokenExpireTime: 1800   #token失效时间，默认30分钟,单位为秒
  intergrateConfig: 
    enable: true   #使用集成的角色、菜单管理功能，将会自动生成三张表，提供增删改查接口
  securityConfig:
    enableRequestLog: true #开启请求日志打印
    bindIp: false #需要token绑定IP时可设为true
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

拦截器通过接口标记的权限码验证，如@PermissionMapping的value=TEST,下面的test接口中@Permission配置的是OptionType.LIST，那么权限码就是： TEST:LIST

权限码可自定义，例如: `@Permission(name = "测试接口",value = OptionType.CUSTOM,customPermissionCode = "MYCUSTOM")`，此时你需要在菜单管理的权限码填上：`TEST:MYCUSTOM`,将其配置给该用户即表示它拥有此权限

当配置 `value=OptionType.LOGIN` 时，表示只要拥有合法token即可访问

一个简单的示例如下：

```java
@RequestMapping("menus")
@RestController
@PermissionMapping(value="MENU")
public class MenuController {
	@Permission(name = "菜单列表",value = OptionType.LIST)
	@GetMapping
	public ResultDTO list(){
		//...                                          
	}
}
```

上面示例中的权限码则为MENU:LIST，权限码用于填写在菜单字段里作为该接口的唯一标识

#### 为普通用户配置权限

**如果你不需要为每个用户指定权限，可以跳过这一步，直接看最后一步**

普通用户需要在菜单管理中添加你在 `@Permission`注解上写的权限码，然后将该菜单的权限赋予用户它才能合法访问，**超级管理员不受此限制**

##### 添加菜单示例

```java
	@Autowired
  	AuthorizeManager authorizeManager;

	@Permission(name = "添加菜单",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addMenu(@Validated @RequestBody MenuDTO menu,@RequestParam(value="parentId",required = false) Long parentId){
		//...
		//parentId是父级菜单ID，可不传
		authorizeManager.getMenuAccess().insertMenu(menu,parentId)
		//...
	}
```

MenuDTO对象内容：

```java
public class MenuVO implements Serializable{
	/**
	 * 修改数据时需要传ID
	 */
	private Long id;

	private String name;

	private String url;

	private String icon;

	/**
	 * 访问该菜单所需的权限码，配置为@PermissionMapping + @Permission的值，如 USER:ADD
	 */
	private String permissionCodes;

	private String description;

	private String property;

	//省略get set
```

##### 为用户绑定菜单代码如下示例

```java
  @Autowired
  AuthorizeManager authorizeManager;

  //...
  @Permission(name = "添加角色",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addRole(@Validated @RequestBody RoleDTO roleDTO){
		//保存角色时绑定菜单ID
		authorizeManager.getRoleAccess().insert(roleDTO,roleDTO.getMenuIds()));
		//...
		//或者修改角色时改变菜单ID
		authorizeManager.getRoleAccess().updateById(roleDTO, roleDTO.getMenuIds()
	}
```

RoleDTO内容：

```java
  public class RoleDTO extends io.github.reinershir.auth.core.model.Role{
	//前端传过来的菜单ID
	private ArrayList<Long> menuIds;

	public ArrayList<Long> getMenuIds() {
		return menuIds;
	}

	public void setMenuIds(ArrayList<Long> menuIds) {
		this.menuIds = menuIds;
	} 
}
```

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
		String token = authorizeManager.generateToken(userId, userType); //如果ID={配置的administratorId}，会拥有所有权限
		//如果使用了集成菜单和角色管理，可通过此方法获取该用户所绑定的菜单权限
		List<Menu> menus = authorizeManager.getMenusByUser(userId);
		return token;
	}
}
```

前端传token时需要在http header里添加：  Access-Token: 登陆接口返回的token  ,header name是可配置的，默认Access-Token

要配置Header Name:

```yml
lui-auth:
  authrizationConfig: 
    tokenHeaderName: X-Access-Token
```

# 其它说明

## 获取Token中的用户ID

首先注入对象

```java
@Autowired(required = false)
AuthorizeManager authorizeManager;

```

根据request对象获取

```java
@GetMapping
public Result<String> getUserId(HttpServletRequest request){
		String userId = authorizeManager.getTokenInfo(request).getUserId();
}
```

## Token IP绑定模式

配置文件中：

```yml
lui-auth:
  securityConfig:
    bindIp: true
```

生成token时将需要绑定的IP传入：

```java
//SecurityUtil.getIpAddress(request) 可替换为你需要绑定的IP
String token = authorizeManager.generateToken(userId,userType,SecurityUtil.getIpAddress(request));
```

## 自动生成表

`intergrateConfig.enable=true` 开启时会自动生成3张表，分别为角色表、菜单表、角色权限表，3张表提供增删改查接口

*跳过权限验证：*

* 1、控制器和方法上都不加注解
* 2、控制器类上加了注解，使用注解跳过单个接口,示例：

```java
@Permission(OptionType.SKIP)
public Result<String> login(){
  //...
}
```

## 角色、菜单集成功能使用示例

### 角色表增删改查接口示例

```java
@RequestMapping("roles")
@RestController
@PermissionMapping(value=ROLE)
public class RoleController {


	RoleAccess roleAccess;
	@Autowired
	public RoleController(AuthorizeManager authorizeManager) {
		this.roleAccess=authorizeManager.getRoleAccess();
	}

	@Permission(name = "角色列表",value = OptionType.LIST)
	@GetMapping
	public ResultDTO<PageBean<Role>> list(@Validated PageReqDTO reqDTO){
		List<io.github.reinershir.auth.core.model.Role> list = roleAccess.selectList(reqDTO.getPage(), reqDTO.getPageSize());
		Long count = roleAccess.selectCount(null);
		return ResponseUtil.generateSuccessDTO(new PageBean<>(reqDTO.getPage(),reqDTO.getPageSize(),count,list));
	}

	@Permission(name = "添加角色",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addRole(@Validated @RequestBody RoleDTO dto){
		if(roleAccess.insert(dto,dto.getRolePermissions())>0) {
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
@RequestMapping("menus")
@RestController
@PermissionMapping(value=MENU)
public class MenuController {


	MenuAccess MenuAccess;
	@Autowired
	public MenuController(AuthorizeManager authorizeManager) {
		this.MenuAccess=authorizeManager.getMenuAccess();
	}

	@Permission(name = "菜单列表",value = OptionType.LIST)
	@GetMapping
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

	@Permission(name = "移动菜单",value = OptionType.UPDATE)
	@PatchMapping("/position")
	public ResultDTO<Object> updateMenu(@RequestBody @Validated  MenuMoveDTO dto){
		boolean flag = false;
		Long moveId = dto.getMoveId();
		Long targetId = dto.getTargetId();
		switch(dto.getPosition()) {
		case 1:
			flag = MenuAccess.moveNodeBefore(moveId, targetId)>0?true:false;
			break;
		case 2:
			flag = MenuAccess.moveNodeAfter(moveId, targetId)>0?true:false;
			break;
		case 3:
			flag = MenuAccess.moveNodeByParentAsLastChild(moveId, targetId)>0?true:false;
			break;
		}
		if(flag) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("修改失败！");
	}
}

public class MenuMoveDTO {

	@NotNull
	@ApiModelProperty(value = "被移动菜单ID",notes = "",  required = true, example = "1")
	private Long moveId;

	@NotNull
	@ApiModelProperty(value = "目标菜单ID",notes = "",  required = true, example = "11")
	private Long targetId;

	@NotNull
	@ApiModelProperty(value = "移动到目标菜单的位置,1=目标前面，2=目标后面，3=目标的子节点最后一个",notes = "1=目标前面，2=目标后面，3=目标的子节点最后一个",  required = true, example = "1")
	private int position;
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

*只验证Token是否有效示例：*

```java
@PermissionMapping(自定义填写)
@Permission(OptionType.LOGIN)
public class RoleController{
}
```

### 开启IP限流功能

添加如下配置

```yml
lui-auth:
  securityConfig:
    enableRequestLimit: true
    requestTime: 3000
    requestLimit: 1
#	requestLimitStorage: memory #IP限流缓存可选：memory、redis，建议memory内存存储，集群服务建议用redis存储
```

以上配置为开启全局IP限制，即每个IP 3秒内同一个接口只能请求一次

*针对单个接口/控制器的IP限流配置：* `@RequestLimit(requestLimit = 1,requestTime = 3000)` 可加在控制器类或方法上（优先使用方法上的注解）

### 自动打印请求日志

添加如下配置：

```yml
lui-auth:
  securityConfig:
    enableRequestLog: true
```

开启后会自动打印请求IP、用户ID、请求参数、请求URI等信息

*自定义日志打印类(需要实现RequestLogger接口)：*

```java
@Configuration
public class WebConfig{

	@Bean
	public RequestLogger initRequestLogger(){
		return new MyRequestLogger();  //返回自己定义的日志处理类，该类需要实现RequestLogger接口
	}

	public MyRequestLogger implements RequestLogger{

		@Override
		public void processRequestLog(HttpServletRequest request, RequestLog requestLog) {
			// ......
		
		}
	  
	}
}
```

当开启自动日志打印开关时拦截器会自动包装HttpServletRequest类，使其IO流可重复读取

# UPDATE Log

*1.2.4* 修复BUG，新增IP绑定模式,更新获取IP方法

*1.2.3* 修复bug，新增mysql8支持

*1.2.2* 新增PostgreSql支持

*1.0.1* 修复了大部分BUG，目前可投入项目中正常使用，修改用户角色关系数据保存在数据中

*0.1.1* 优化请求日志功能，增加token中附带用户信息

*0.10* 增加IP限制功能、增加请求日志自动打印功能

*0.0.3* 增加角色、菜单权限管理功能

*0.0.1* 简单的权限验证、token验证功能

# TODO LIST

1、独立为一个单独的鉴权服务，支持通过注册中心、HTTP等调用方式	`<br/>`

2、IP白黑名单	`<br/>`

3、数据权限（构想中...）	`<br/>`

4、支持redisson

5、增加支持的数据库

6、恶意IP/域名知识库

7、IP与TOKEN绑定

8、非对称加密请求参数

# 初始化表结构

如开启了 `intergrateConfig.enable=true`，会自动生成表，无需手动建表

**PostgreSql**

```sql
CREATE TABLE public.MENU (
  ID serial4 NOT NULL ,
  NAME varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  URL varchar(200) COLLATE "pg_catalog"."default",
  ICON varchar(300) COLLATE "pg_catalog"."default",
  PERMISSION_CODES varchar(150) COLLATE "pg_catalog"."default",
  DESCRIPTION varchar(255) COLLATE "pg_catalog"."default",
  LEFT_VALUE int4 NOT NULL,
  RIGHT_VALUE int4 NOT NULL,
  LEVEL int2 NOT NULL,
  PROPERTY varchar(100) COLLATE "pg_catalog"."default",
  CREATE_DATE date NOT NULL,
  UPDATE_DATE date,
  PRIMARY KEY ("ID")
)
;

COMMENT ON COLUMN public.MENU."URL" IS '跳转地址';

COMMENT ON COLUMN public.MENU."ICON" IS '图标';

COMMENT ON COLUMN public.MENU."PERMISSION_CODES" IS '权限码';

COMMENT ON COLUMN public.MENU."DESCRIPTION" IS '说明 ';

COMMENT ON COLUMN public.MENU."LEFT_VALUE" IS '左节点值';

COMMENT ON COLUMN public.MENU."RIGHT_VALUE" IS '右节点值';

COMMENT ON COLUMN public.MENU."LEVEL" IS '节点等级';

COMMENT ON COLUMN public.MENU."PROPERTY" IS '属性(自由使用标识)';

COMMENT ON COLUMN public.MENU."CREATE_DATE" IS '创建时间';

COMMENT ON COLUMN public.MENU."UPDATE_DATE" IS '修改时间';

COMMENT ON TABLE public.MENU IS '菜单表';




CREATE TABLE public.ROLE (
  ID serial4 NOT NULL,
  ROLE_NAME varchar(100) COLLATE "pg_catalog"."default" NOT NULL,
  DESCRIPTION varchar(200) COLLATE "pg_catalog"."default",
  CREATE_DATE date NOT NULL,
  UPDATE_DATE date,
  PRIMARY KEY ("ID")
)
;


COMMENT ON COLUMN public.ROLE."ID" IS 'ID';

COMMENT ON COLUMN public.ROLE."ROLE_NAME" IS '角色名称';

COMMENT ON COLUMN public.ROLE."DESCRIPTION" IS '  说明';

COMMENT ON COLUMN public.ROLE."CREATE_DATE" IS '创建时间';

COMMENT ON COLUMN public.ROLE."UPDATE_DATE" IS '修改时间';

COMMENT ON TABLE public.ROLE IS '角色表';

-- 关系表

CREATE TABLE public.ROLE_USER (
  ID serial4,
  ROLE_ID int8 NOT NULL,
  USER_ID varchar COLLATE pg_catalog.default NOT NULL
);


CREATE TABLE public.ROLE_MENU (
  ID serial4,
  ROLE_ID int8 NOT NULL,
  MENU_ID int8 NOT NULL,
  PERMISSION_CODES varchar(150),
  PRIMARY KEY (ID)
)
;


```
