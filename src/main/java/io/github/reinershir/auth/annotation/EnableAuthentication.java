package io.github.reinershir.auth.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import io.github.reinershir.auth.config.AutoConfig;

/**
 * @date:   2019年5月16日 下午5:24:26   
 * @author reinershir
 * @Description: 开启权限验证开关,权限注解用法：@HasPermission(OptionType.All), 服务权限注解用法：@ServicePermission({serviceIds=""})
 * 支持controller或方法
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(AutoConfig.class)
public @interface EnableAuthentication {

}
