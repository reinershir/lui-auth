package io.github.reinershir.auth.sample.controller;

import io.github.reinershir.auth.annotation.OptionType;
import io.github.reinershir.auth.annotation.Permission;
import io.github.reinershir.auth.annotation.PermissionMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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