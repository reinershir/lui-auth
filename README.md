# lui-auth

<div align="center">
  <p>
  </p>

[**简体中文**](README.zh.md) |**English** | [**日本語**](README.jp.md) 

</div>

A simple permission verification tool that relies on Spring Boot, integrating role, menu, and permission functionalities. No need to download the project or configure complex settings. Just rely on the JAR file and make simple configurations to use it. It is very useful when you don't want to use any scaffolding or complex dependencies.


The features are as follows:

1. Simple configuration.

2. Integration of menu, role, and permission management.

3. Support for only one person logging in with the same account.

4. Use annotations to mark permissions and reduce code intrusion.

5. Use Redis to store permission information.

6. Menu management supports an unlimited hierarchical tree structure stored using modified preorder tree traversal, resulting in highly efficient queries.
  
#### Preconditions

* Spring Boot 2.0 +

* Redis 5.0 +

* spring-boot-starter-data-redis 

* JDK 1.8 +
  
* MYSQL 5.7+ OR ORACLE 

#### Examp

https://github.com/reinershir/lui-auth-examp
  
# Get start

## Dependency
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
if using spring boot 3.0 + :
```xml
<dependency>
	<groupId>io.github.reinershir.auth</groupId>
	<artifactId>lui-auth</artifactId>
	<version>2.0.1-RELEASE</version>
</dependency>

<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```


## Step.1 Add annotation switch to the startup class.

Add the @EnableAuthentication annotation switch to your project's startup class.
```java
@SpringBootApplication
@EnableAuthentication
public class Application {
	
	public static void main(String[] args) {
		Application.run(Application.class, args);
	}
}
```

## Step.2 Configure Redis connection information and token encryption key

```yml
spring:
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
    password: pwd123
    timeout: 3000

lui-auth:
  authrizationConfig: 
    administratorId: 1  #Super admin user ID, to prevent the situation where roles are deleted and unable to log in, this user ID will have all permissions as soon as they log in.
    tokenSalt: yorTokenSalt   # token salt
    tokenExpireTime: 1800   
  intergrateConfig: 
    enable: true   #By using the integrated role and menu management function, three tables will be automatically generated to provide interfaces for adding, deleting, modifying, and querying.
  securityConfig:
    enableRequestLog: true #Enable request log printing.
    bindIp: false #When token binding to IP is required, it can be set to true.
```



## Step.3 Configure Interceptors

The following is the configuration method for Spring Boot：
```java
@EnableWebMvc
public class WebMvcConfig  implements WebMvcConfigurer {

	@Autowired(required=false)
	AuthenticationInterceptor authenticationInterceptor;
	
	/**
	 * addInterceptors
	 */
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
		if(authenticationInterceptor!=null){
			registry.addInterceptor(authenticationInterceptor);
		}
    }
```

## Step.4 Add annotation tags to the interfaces that require authentication.

Taking the controller as an example：

```java
@RequestMapping("test")
@RestController
@PermissionMapping(value = "TEST")
public class ExampController {

	@Permission(name = "your API name,it can be null",value = OptionType.LIST)
	@GetMapping("testRedis")
	public Object test(String param) {
		return "";
	}
}
```

## Adding annotation tags to interfaces that require authentication

The interceptor verifies the permission code marked by the interface. For example, if the value of @PermissionMapping is TEST and the @Permission configured in the test interface is OptionType.LIST, then the permission code will be: TEST:LIST.

The permission code can be customized, for example: `@Permission(name = "Test Interface", value = OptionType.CUSTOM, customPermissionCode = "MYCUSTOM")`. In this case, you need to fill in `TEST:MYCUSTOM` as the permission code in menu management and assign it to users who have this permission.

When configuring `value=OptionType.LOGIN`, it means that accessing only requires a valid token.

Here's a simple example:

```java
@RequestMapping("menus")
@RestController
@PermissionMapping(value="MENU")
public class MenuController {

@Permission(name = "Menu List", value = OptionType.LIST)
@GetMapping
public ResultDTO list(){
//...
}
}

```

In the above example, the permission code would be MENU:LIST. The permission code is used as a unique identifier for this interface in menu fields.

#### Configuring Permissions for Regular Users

**If you don't need to specify permissions for each user, you can skip this step and proceed to the final step.**

Regular users need to add the permission code that you have written in the `@Permission` annotation in the menu management. Then assign the permission of that menu to the user so that they can access it legally. **Super administrators are not subject to this restriction.**

##### Example of Adding a Menu
```java
	@Autowired
  	AuthorizeManager authorizeManager;

	@Permission(name = "Add Menu",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addMenu(@Validated @RequestBody MenuDTO menu,@RequestParam(value="parentId",required = false) Long parentId){
		//...
		//parentId is the ID of the parent menu, it can be omitted.
		authorizeManager.getMenuAccess().insertMenu(menu,parentId)
		//...
	}
```
MenuDTO：
```java
public class MenuVO implements Serializable{
	private Long id;
	private String name;
	private String url;
	private String icon;
	/**
	 * To access the required permission code for this menu, configure it as @PermissionMapping + @Permission value, such as USER:ADD.
	 */
	private String permissionCodes;
	private String description;
	private String property
	//omission get set
```

##### To bind menu code for users, please refer to the following example.
```java
  @Autowired
  AuthorizeManager authorizeManager;

  //...
  @Permission(name = "ADD Role",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addRole(@Validated @RequestBody RoleDTO roleDTO){
		//Save the role and bind it with menu ID.
		authorizeManager.getRoleAccess().insert(roleDTO,roleDTO.getMenuIds()));
		//...
		//Or Update
		authorizeManager.getRoleAccess().updateById(roleDTO, roleDTO.getMenuIds()
	}
```
RoleDTO：
```java
  public class RoleDTO extends io.github.reinershir.auth.core.model.Role{
	//The menu ID passed from the front-end.
	private ArrayList<Long> menuIds;

	public ArrayList<Long> getMenuIds() {
		return menuIds;
	}

	public void setMenuIds(ArrayList<Long> menuIds) {
		this.menuIds = menuIds;
	} 
}
```

##  The final step,Generate token

After verifying the account and password in the login interface, call the following interface to generate a token and return it to the front end:

```java
@RestController
@RequestMapping("user")
public class LoginController {
@Autowired
AuthorizeManager authorizeManager;
@PostMapping("login")
public Object login(@RequestBody LoginInfoDTO loginInfo) {
	// After completing the login verification
	String userId = "Your unique user ID";
	Sint userType = 1; // User type flag
	String token = authorizeManager.generateToken(userId, userType); // If ID={configured administratorId}, all permissions will be granted.
	// If integrated menu and role management is used, you can use this method to get the menu permissions bound to this user.
	List<Menu> menus = authorizeManager.getMenusByUser(userId);
	return token;
	}
}
```




Front-end needs to add the token in the HTTP header when transmitting. The header name can be configured, with the default value being "Access-Token".

To configure the Header Name:
```yml
lui-auth:
  authrizationConfig: 
    tokenHeaderName: X-Access-Token
```

# Other Instructions

## Get User ID from Token

First inject the object:

```java
@Autowired(required = false)
AuthorizeManager authorizeManager;

```
Get it based on the request object:

```java
@GetMapping
public Result<String> getUserId(HttpServletRequest request){
	String userId = authorizeManager.getTokenInfo(request).getUserId();
}

```

## Token IP Binding Mode

In the configuration file:

```yml
lui-auth:
securityConfig:
bindIp: true
```

When generating a token, pass in the IP that needs to be bound:

```java

//SecurityUtil.getIpAddress(request) can be replaced with the IP you need to bind.

String token = authorizeManager.generateToken(userId,userType,SecurityUtil.getIpAddress(request));

```

## Automatically Generate Tables

`intergrateConfig.enable=true` When enabled, three tables will be automatically generated: role table, menu table, and role permission table. These three tables provide interfaces for CRUD operations.

*Skip Permission Verification:*

* 1. Do not add annotations to both controllers and methods.

* 2. Add annotations to controller classes and use annotations to skip individual interfaces. Example:

```java
@Permission(OptionType.SKIP)
public Result<String> login(){
//...
}

```

## Role and Menu Set Function Usage Example

### Role Table CRUD Interface Example

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

	@Permission(name = "Role List",value = OptionType.LIST)
	@GetMapping
	public ResultDTO<PageBean<Role>> list(@Validated PageReqDTO reqDTO){
		List<io.github.reinershir.auth.core.model.Role> list = roleAccess.selectList(reqDTO.getPage(), reqDTO.getPageSize());
		Long count = roleAccess.selectCount(null);
		return ResponseUtil.generateSuccessDTO(new PageBean<>(reqDTO.getPage(),reqDTO.getPageSize(),count,list));
	}
	
	@Permission(name = "Add Role",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addRole(@Validated @RequestBody RoleDTO dto){
		if(roleAccess.insert(dto,dto.getRolePermissions())>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("failed");
	}
	
	@Permission(name = "Update Role",value = OptionType.UPDATE)
	@PatchMapping
	public ResultDTO<Object> updateUser(@Validated(value = ValidateGroups.UpdateGroup.class) @RequestBody RoleDTO roleDTO){
		if(roleAccess.updateById(roleDTO, roleDTO.getMenuIds())>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("failed");
	}
	
	@Permission(name = "Delete Role",value = OptionType.DELETE)
	@DeleteMapping("/{id}")
	public ResultDTO<Object> delete(@PathVariable("id") Long id){
		if(roleAccess.deleteById(id)>0) {
			return ResponseUtil.generateSuccessDTO("success");
		}
		return ResponseUtil.generateFaileDTO("failed");
	}
	
	@Permission(name = "Query the menu permissions bound to the role.",value = OptionType.CUSTOM,customPermissionCode = "ROLE_PERMISSION")
	@GetMapping("/{roleId}/rolePermissions")
	public ResultDTO<List<RolePermission>> getRolePermissionsById(@PathVariable("roleId") Long roleId){
		return ResponseUtil.generateSuccessDTO(roleAccess.selectRolePermissionByRole(roleId));
	}
}



```

### Menu Table Interface Usage Example

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

	@Permission(name = "Menu List",value = OptionType.LIST)
	@GetMapping
	public ResultDTO<List<Menu>> list(@RequestParam(value="parentId",required = false) Long parentId){
		return ResponseUtil.generateSuccessDTO(MenuAccess.qureyList(parentId));
	}
	
	@Permission(name = "Add Menu",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addMenu(@Validated @RequestBody MenuVO menu,@RequestParam(value="parentId",required = false) Long parentId){
		if(MenuAccess.insertMenu(menu,parentId)>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("failed！");
	}
	
	@Permission(name = "Update Menu",value = OptionType.UPDATE)
	@PatchMapping
	public ResultDTO<Object> updateMenu( @RequestBody MenuVO MenuDTO){
		if(MenuAccess.updateById(MenuDTO)>0) {
			return ResponseUtil.generateSuccessDTO();
		}
		return ResponseUtil.generateFaileDTO("failed！");
	}
	
	@Permission(name = "Delete Menu",value = OptionType.DELETE)
	@DeleteMapping("/{id}")
	public ResultDTO<Object> delete(@PathVariable("id") Long id){
		if(MenuAccess.deleteById(id)>0) {
			return ResponseUtil.generateSuccessDTO("success！");
		}
		return ResponseUtil.generateFaileDTO("failed！");
	}

	@Permission(name = "Moveing Menu",value = OptionType.UPDATE)
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
		return ResponseUtil.generateFaileDTO("failed！");
	}
}

public class MenuMoveDTO {
	@NotNull
	@ApiModelProperty(value = "ID of the menu to be moved", notes = "", required = true, example = "1")
	private Long moveId;
	@NotNull
	@ApiModelProperty(value = "ID of the target menu", notes = "", required = true, example = "11")
	private Long targetId;
	@NotNull
	@ApiModelProperty(value = "Position to move to in the target menu, 1=before the target, 2=after the target, 3=last child node of the target", notes ="1=before the target, 2=after the target, 3=last child node of the target", required=true ,example="1")
	private int position;
}
```

### Binding Roles for Users Example

```java
@Autowired
AuthorizeManager authorizeManager;

...
//binding role
if(!CollectionUtils.isEmpty(roleIds)) {
	authorizeManager.getRoleAccess().bindRoleForUser(userId, roleIds);
}


//Get the roles that are bound to the user
authorizeManager.getRoleAccess().getRoleByUser(userId);
```

*Only validate the example of whether the token is valid:*
```java
@PermissionMapping(YOURCODE)
@Permission(OptionType.LOGIN)
public class RoleController{
}
```

### Enable IP Rate Limiting Function

Add the following configuration
```yml
lui-auth:
  securityConfig:
    enableRequestLimit: true
    requestTime: 3000
    requestLimit: 1
#	requestLimitStorage: memory #IP rate limiting cache options: memory and redis are available. It is recommended to use memory for storage, while redis is suggested for cluster services.
```

The above configuration is for enabling global IP restriction, which means that each IP can only request the same interface once within 3 seconds.

*For rate limiting configuration on a single interface/controller:*

`@RequestLimit(requestLimit = 1,requestTime = 3000)` can be added to the controller class or method (preferably using the annotation on the method).

### Automatic printing of request logs

Add the following configuration:
```yml
lui-auth:
  securityConfig:
    enableRequestLog: true
```

After enabling, it will automatically print information such as the requested IP, user ID, request parameters, and request URI.

*Custom log printing class (needs to implement the RequestLogger interface):*

```java
@Configuration
public class WebConfig{

	@Bean
	public RequestLogger initRequestLogger(){
		return new MyRequestLogger();  //Return your own defined log processing class, which needs to implement the RequestLogger interface.
	}
	
	public MyRequestLogger implements RequestLogger{
	
		@Override
		public void processRequestLog(HttpServletRequest request, RequestLog requestLog) {
			// ......
			
		}
	  
	}
}
```
When the automatic log printing switch is turned on, the interceptor will automatically wrap the HttpServletRequest class to make its IO stream repeatable.

# UPDATE Log

*1.2.4* Fixed bugs and added IP binding mode, updated IP acquisition method.

*1.2.3* Fixed bug and added support for MySQL 8.

*1.2.2* Added support for PostgreSql.

*1.0.1* Fixed most of the bugs, currently can be used normally in projects, modified user role relationship data saved in database.

*0.1.1* Optimized request logging function, added user information attached to token.

*0.10* Added IP restriction function and automatic request logging function.

*0.0.3 *Added role and menu permission management function.

*0 . 01 * Simple authorization verification and token verification functions.

# TODO LIST

1、Independently as a separate authentication service, supporting invocation through registry center, HTTP, etc.<br/>

2、IP whitelist/blacklist<br/>

3、Data permissions (in conception...)<br/>

4、Support Redisson

5、Add supported databases

6、Malicious IP/domain knowledge base

7、Binding of IP with TOKEN

8、Asymmetric encryption of request parameters

# Initialize table structure

If `intergrateConfig.enable=true` is enabled, tables will be generated automatically without manual creation.

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

COMMENT ON COLUMN public.MENU."URL" IS 'Redirect address';

COMMENT ON COLUMN public.MENU."ICON" IS 'icon';

COMMENT ON COLUMN public.MENU."PERMISSION_CODES" IS 'Permission code';

COMMENT ON COLUMN public.MENU."DESCRIPTION" IS ' ';

COMMENT ON COLUMN public.MENU."LEFT_VALUE" IS '';

COMMENT ON COLUMN public.MENU."RIGHT_VALUE" IS '';

COMMENT ON COLUMN public.MENU."LEVEL" IS '';

COMMENT ON COLUMN public.MENU."PROPERTY" IS 'Attribute (Free Use Identifier)';

COMMENT ON COLUMN public.MENU."CREATE_DATE" IS '';

COMMENT ON COLUMN public.MENU."UPDATE_DATE" IS '';

COMMENT ON TABLE public.MENU IS '';




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

COMMENT ON COLUMN public.ROLE."ROLE_NAME" IS '';

COMMENT ON COLUMN public.ROLE."DESCRIPTION" IS '  ';

COMMENT ON COLUMN public.ROLE."CREATE_DATE" IS '';

COMMENT ON COLUMN public.ROLE."UPDATE_DATE" IS '';

COMMENT ON TABLE public.ROLE IS '';

-- Intermediate table

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



