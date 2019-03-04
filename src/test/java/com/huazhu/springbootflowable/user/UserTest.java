package com.huazhu.springbootflowable.user;

import com.huazhu.springbootflowable.SpringbootFlowableApplicationTests;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.IdmIdentityService;
import org.flowable.idm.api.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class UserTest extends SpringbootFlowableApplicationTests {

    @Autowired
    private IdentityService identityService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private IdmIdentityService idmIdentityService;

    @Test
    public void createUser() {
        User user = identityService.newUser("user:2");
        user.setTenantId("corp:1");
        user.setEmail("100000@qq.com");
        user.setFirstName("赵");
        user.setLastName("欢");
        user.setPassword("1234");
        identityService.saveUser(user);


    }

    @Test
    public void createGroup() {
        Group group = identityService.newGroup("group1");
        group.setName("2组");
        //identityService.saveGroup(group);
        identityService.createMembership("user:1","group1");
    }



    @Test
    public void getUser() {
        List<User> list = identityService.createUserQuery().tenantId("corp:1").list();
        list.forEach(user -> {
            log.info("user_id:{}",user.getId());
            log.info("username:{}{}",user.getFirstName(),user.getLastName());
        });
    }
}
