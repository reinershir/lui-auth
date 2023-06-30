# lui-auth

<div align="center">
  <p>
  </p>

[**简体中文**](README.zh.md) |[**English**](README.md) | **日本語**

</div>

Spring Bootのシンプルな認証ツールで、ロール、メニュー、権限の機能を統合しています。プロジェクトのダウンロードや複雑な設定は必要ありません。単にjarファイルに依存し、簡単な設定を行うだけで使用することができます。フレームワークや複雑な依存関係を使いたくない場合に非常に便利です。

以下は特徴です：<br/>

1. シンプルな設定<br/>

2. メニュー、ロール、権限管理の統合<br/>

3. 同じアカウントで一人しかログインできない制約サポート<br/>

4. アノテーションを使用した権限マーキングによるコードへの影響低減<br/>

5. 権限情報の保存にRedisを使用<br/>

6. 無制限階層ツリー形式のメニュー管理（左右値木構造）で効率的なクエリ処理

#### 前提条件

* Spring Bootバージョン2.0以上

* Redisバージョン5.0以上

* spring-boot-starter-data-redis依存関係

* JDKバージョン1.8以上

* MYSQLバージョン5.7以上またはORACLE

#### サンプル

シンプルな例のリポジトリ：https://github.com/reinershir/lui-auth-examp

# 使用開始

## 依存関係を追加
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

## 起動クラスにアノテーションのスイッチを追加

プロジェクトの起動クラスに@EnableAuthenticationアノテーションのスイッチを追加してください。
```java
@SpringBootApplication
@EnableAuthentication
public class Application {
	
	public static void main(String[] args) {
		Application.run(Application.class, args);
	}
}
```

## Redisの接続情報とトークンの暗号化キーを設定する

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
    administratorId: 1 # スーパーアドミンユーザーID。ロールが全て削除された場合でもログインできるようにするため、このユーザーIDはすべての権限を持つ。
    tokenSalt: yorTokenSalt # トークン生成用ソルト
    tokenExpireTime: 1800 # デフォルトでは30分後にトークンが無効になる。単位は秒。
  intergrateConfig:
    enable:true # 統合されたロール・メニュー管理機能を使用する場合、3つのテーブルが自動的に作成され、CRUDインタフェースが提供されます。
  securityConfig:
    enableRequestLog:true　# リクエストログ出力を有効化します。
    bindIp:false　# IPアドレスごとに異なるトークンを必要とする場合はtrueに設定してください。
```

## インターセプタの設定方法

以下はSpring Bootでの設定方法です：
```java
@EnableWebMvc
public class WebMvcConfig  implements WebMvcConfigurer {

	@Autowired(required=false)
	AuthenticationInterceptor authenticationInterceptor;
	
	@Override
    public void addInterceptors(InterceptorRegistry registry) {
		if(authenticationInterceptor!=null){
			registry.addInterceptor(authenticationInterceptor);
		}
    }
```

## 鉴权が必要なインターフェースに注釈を追加する

インターセプターは、@PermissionMappingのvalue=TESTのような権限コードを使用して、インターフェースにマークされた権限を検証します。例えば、testインターフェースで@PermissionがOptionType.LISTに設定されている場合、権限コードは次のようになります： TEST:LIST

権限コードはカスタマイズ可能です。例えば、「@Permission(name = "テストインターフェース", value = OptionType.CUSTOM, customPermissionCode = "MYCUSTOM")」というように指定することもできます。この場合、メニュー管理の権限コード欄に「TEST:MYCUSTOM」と入力し、ユーザーにこの権限を付与することができます。

「value=OptionType.LOGIN」と設定されている場合は、有効なトークンさえ持っていればアクセス可能です。

以下は簡単な例です：
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
上記の例では、権限コードはMENU:LISTであり、このインターフェースを一意に識別するためにメニューフィールドに入力されます。

#### 一般ユーザーの権限設定

**各ユーザーに対して個別の権限を割り当てる必要がない場合は、このステップをスキップし、最後のステップに進んでください**

一般ユーザーは、`@Permission`アノテーションで指定した権限コードをメニュー管理に追加し、そのメニューの権限を付与されることで正当なアクセスが可能となります。ただし、「超級管理者」はこの制約の影響を受けません。

##### メニュー追加例
```java
	@Autowired
  	AuthorizeManager authorizeManager;

	@Permission(name = "添加菜单",value = OptionType.ADD)
	@PostMapping
	public ResultDTO<Object> addMenu(@Validated @RequestBody MenuDTO menu,@RequestParam(value="parentId",required = false) Long parentId){
		//...
		//parentIdは親メニューのIDであり、省略可能です。
		authorizeManager.getMenuAccess().insertMenu(menu,parentId)
		//...
	}
```
MenuDTO对象内容：
```java
public class MenuVO implements Serializable{

	private Long id;
	
	private String name;
	
	private String url;
	
	private String icon;
	
	/**
	 * メニューにアクセスするための権限コードは、@PermissionMapping + @Permissionの値で設定されます。例えば、USER:ADDです。
	 */
	private String permissionCodes;
	
	private String description;
	
	private String property;

	//省略get set
```

##### ユーザーにメニューコードをバインドする例は以下の通りです。
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

## 最後のステップ、トークンの生成

ログインインターフェースでアカウントとパスワードの検証が完了した後、以下のインターフェースを呼び出してトークンを生成し、フロントエンドに返します。

```java
@RestController
@RequestMapping("user")
public class LoginController {
@Autowired
AuthorizeManager authorizeManager;
@PostMapping("login")
public Object login(@RequestBody LoginInfoDTO loginInfo) {
  // ログイン検証が完了したら
  String userId = "ユーザーID";
  Sint userType = 1; // ユーザータイプマーク
  String token = authorizeManager.generateToken(userId, userType); // ID={administratorId} の場合はすべての権限を持つことになります。
  // 統合メニューや役割管理を使用している場合、このメソッドでユーザーに関連付けられたメニュー権限を取得することができます。
  List<Menu> menus = authorizeManager.getMenusByUser(userId);
  return token;
}

}

```

フロントエンドからトークンを送信する際は、httpヘッダに次のように追加する必要があります：Access-Token: ログインインターフェースから返されたトークン。デフォルトではHeader Nameは"Access-Token"です。

Header Name を設定する必要があります。
```yml
lui-auth:
  authrizationConfig: 
    tokenHeaderName: X-Access-Token
```

# その他の説明

## トークンからユーザーIDを取得する方法

まず、オブジェクトをインジェクションします。

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

## トークンIPバインディングモード

設定ファイル内：

```yml
lui-auth:
  securityConfig:
    bindIp: true
```

生成token時には、バインドする必要のあるIPを渡します：

```java

//SecurityUtil.getIpAddress(request) は、バインドするIPに置き換えられます
String token = authorizeManager.generateToken(userId,userType,SecurityUtil.getIpAddress(request));
```


## 自動生成テーブル

`intergrateConfig.enable=true` を有効にすると、3つのテーブルが自動的に生成されます。それぞれ役割テーブル、メニューテーブル、役割権限テーブルであり、これらのテーブルは追加、削除、更新、検索のインタフェースを提供します。

*権限チェックをスキップする方法：*

* 1. コントローラーおよびメソッドに注釈を付けない

* 2. コントローラークラスに注釈を付けて個別のインタフェースをスキップする,例：

```java
@Permission(OptionType.SKIP)
public Result<String> login(){
  //...
}
```

## ロール、メニューセットの機能使用例

### ロールテーブルの追加、削除、変更、検索インターフェースの例

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

### メニューテーブルインタフェースの使用例

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
  @ApiModelProperty(value = "移動されるメニューのID",notes = "", required = true, example = "1")
  private Long moveId;
  @NotNull
  @ApiModelProperty(value = "目標メニューのID",notes = "", required = true, example = "11")
  private Long targetId;
  @NotNull
  @ApiModelProperty(value = "目標メニューへの位置、1=目標の前、2=目標の後ろ、3=目標の最後の子ノード",notes = "1=目標の前、2=目標の後ろ、3=目標の最後の子ノード", required = true, example ="1")
  private int position;
}
```

### ユーザーに役割をバインドする例

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

*トークンの有効性を確認するだけの例：*
```java
@PermissionMapping(自定义填写)
@Permission(OptionType.LOGIN)
public class RoleController{
}
```

### IP制限機能を有効にする

以下の設定を追加してください。
```yml
lui-auth:
  securityConfig:
    enableRequestLimit: true
    requestTime: 3000
    requestLimit: 1
#	requestLimitStorage: memory #IP制限のキャッシュオプション：memory、redis。memoryはメモリストレージをお勧めしますが、クラスターサービスではredisストレージを使用することをお勧めします。
```

上記の設定は、グローバルIP制限を有効にするものであり、つまり、同じインターフェースに対して3秒以内に同じIPからのリクエストは1回しか行えません。

*個別のインターフェース/コントローラーのIP制限設定：* `@RequestLimit(requestLimit = 1,requestTime = 3000)` をコントローラークラスまたはメソッドに追加します（メソッドのアノテーションが優先されます）

### リクエストログを自動的に出力する

以下の設定を追加してください：
```yml
lui-auth:
  securityConfig:
    enableRequestLog: true
```
開始すると、IPアドレス、ユーザーID、リクエストパラメータ、リクエストURIなどの情報が自動的に印刷されます。

*カスタムログ出力クラス（RequestLoggerインターフェースを実装する必要があります）：*

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

自動ログ出力スイッチをオンにすると、インターセプターはHttpServletRequestクラスを自動的にラップし、そのIOストリームを繰り返し読み取ることができます。

# ログの更新

*1.2.4* バグ修正、IPバインディングモードの追加、IP取得方法の更新

*1.2.3* バグ修正、MySQL 8サポートの追加

*1.2.2* PostgreSQLサポートの追加

*1.0.1* 多くのバグ修正。現在はプロジェクトで正常に使用できます。ユーザーの役割関係データがデータベースに保存されるように変更されました。

*0.1.1* リクエストログ機能を最適化し、トークンにユーザー情報を含めるようにしました。

*0.10* IP制限機能およびリクエストログ自動印刷機能を追加しました。

*0.0.3 * 役割およびメニューのアクセス許可管理機能を追加しました。

* 0 .01 * シンプルな認証およびトークン認証機能

＃TODOリスト

1つ目：独立した認証サービスとして独立し、レジストリセンター、HTTPなどの呼び出し方法をサポートします。<br/>

2つ目：IPホワイトリスト/ブラックリスト<br/>

3番目：データアクセス権限（コンセプト中...）<br/>

4番目：redissonのサポートを追加する

5番目：対応するデータベースを追加する

6番目：マルウェアIP /ドメインナレッジベース

7番目: IPとTOKENのバインド

8番目: 非対称暗号化要求パラメータ

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


