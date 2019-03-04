package com.huazhu.springbootflowable.common.base;

import com.huazhu.springbootflowable.util.ObjectUtils;
import org.flowable.idm.api.User;

import java.util.Map;

public class BaseUser implements User {

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private String tenantId;

    public BaseUser() {
    }

    public BaseUser(Map<String, Object> userDetails) {
        this.userId = ObjectUtils.toString(userDetails.get("userId"));
        this.firstName = ObjectUtils.toString(userDetails.get("realName")).substring(0,1);
        this.lastName = ObjectUtils.toString(userDetails.get("realName")).substring(1);
        this.email = "1003873553@qq.com";
        this.tenantId = ObjectUtils.toString(userDetails.get("corpId"));
    }


    @Override
    public String getId() {
        return this.userId;
    }

    @Override
    public void setId(String s) {
        this.userId = s;
    }

    @Override
    public String getFirstName() {
        return this.firstName;
    }

    @Override
    public void setFirstName(String s) {
        this.firstName = s;
    }

    @Override
    public void setLastName(String s) {
        this.lastName = s;
    }

    @Override
    public String getLastName() {
        return this.lastName;
    }

    @Override
    public void setDisplayName(String s) {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setEmail(String s) {
        this.email = s;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(String s) {
        this.password = s;
    }

    @Override
    public String getTenantId() {
        return this.tenantId;
    }

    @Override
    public void setTenantId(String s) {
        this.tenantId = s;
    }

    @Override
    public boolean isPictureSet() {
        return false;
    }
}
