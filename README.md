# lui-auth

一个依赖于spring boot简单的权限验证工具，无需下载工程，无复杂配置，只需依赖jar并简单配置即可使用。

特点如下：
  1、配置简单
  2、可动态授权
  3、支持同一账号只能一人登陆
  4、使用注解标记权限，减少代码入侵
  5、使用redis存储权限信息
  
# 开始使用

#### 添加依赖
```xml
		<dependency>
            <groupId>com.xh.auth</groupId>
            <artifactId>lui-auth</artifactId>
            <version>0.0.1-RELEASE</version>
        </dependency>
		
		

```
**maven中央库上传中...**

#### 启动类添加注解开关
在你的项目启动类添加@EnableAuthentication注解开关
```java
@SpringBootApplication
@EnableAuthentication
public class Application {
	
	public static void main(String[] args) {
		Application.run(MonitorApplication.class, args);
	}
}
```

#### 配置redis连接信息和token加密密钥

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
  tokenSalt: 1233211234567 #密钥可自由配置
  tokenHeaderName: Access-Token   #前端传token时的头名称，可修改
  tokenExpireTime: 1800   #token失效时间，默认30分钟,单位为秒
```



#### 配置拦截器

```java
@EnableWebMvc
public class WebMvcConfig  implements WebMvcConfigurer {

	@Autowired
	AuthenticationInterceptor authenticationInterceptor;
	
	/**
	 * 添加拦截器
	 */
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor);
    }
```

#### 给需要鉴权的接口添加注解标记

以controller为例：

```java
@RequestMapping("test")
@RestController
@PermissionMapping(value = "TEST",name = "测试功能管理",parentPermissionCode = "TEST:VIEW",isParentNode = true)
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


#### 最后一步，生成token

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
		String token = authorizeManager.generateToken(userId, userType);
		return token;
	}
}
```

此时的用户是没有权限的，需要给用户授权，如下例子，给ID为“1”的用户授予所有权限：

```java

@RestController
@RequestMapping("user")
public class LoginController {

	@Autowired
	AuthorizeManager authorizeManager;
	
	@PostMapping("login")
	public Object login(@RequestBody LoginInfoDTO loginInfo) {
		//登陆验证完成后
		String userId = "1";
		Sint userType = 1; //用户类型标记
		String token = authorizeManager.generateToken(userId, userType);
		//给ID为1的用户添加所有权限，即“超级管理员”
		authorizeManager.grantTemporaryPermission("1", authorizeManager.getAllPermissionCodes());
		//也可以添加有时效的临时授权
		authorizeManager.grantTemporaryPermission("1", authorizeManager.getAllPermissionCodes(),1800l);
		return token;
	}
}
```

前端传token时需要在http header里添加：  Access-Token: 登陆接口返回的token  ,header name是可配置的，默认Access-Token

用户->角色、角色->权限的关系表自行维护，此工具仅仅用来授权和鉴权，具体思路是，给角色绑定权限码，为用户添加角色时将该角色的权限码授予该用户（暂时还不支持角色修改后其关联用户的权限码一并修改）

#### 其它说明

`authorizeManager.getPermissionMenu()` 可获取在注解上配置的名称和权限码，结构为二级深度的自包含树形结构


初始版本还很渣，后续会渐渐的增加功能并优化，逐渐思考新的鉴权方式

### TODO LIST

1、独立为一个单独的鉴权服务，支持微服务注册中心、对称密钥等调用方式

2、IP白黑名称

3、用户限流

4、支持mysql存储

5、优化权限菜单结构









