package com.huazhu.springbootflowable.modules.idm.controller;

import com.huazhu.basejarservice.UUIDUtils;
import com.huazhu.springbootflowable.common.base.BaseUser;
import com.huazhu.springbootflowable.common.response.R;
import org.flowable.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IdentityService identityService;

    @PostMapping
    public R createUser(@RequestBody BaseUser user) {
        user.setId(UUIDUtils.getId());
        identityService.saveUser(user);
        return R.ok();
    }
}
