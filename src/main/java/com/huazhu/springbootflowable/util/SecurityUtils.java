package com.huazhu.springbootflowable.util;

import com.huazhu.springbootflowable.common.base.BaseUser;
import com.huazhu.user.config.oauth2.LoginUserDetails;
import org.flowable.idm.api.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class SecurityUtils {

    public static User getCurrentUserObject() {
        LoginUserDetails loginUserDetails = (LoginUserDetails)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Map<String, Object> userDetails = loginUserDetails.getUserDetails();
        BaseUser user = new BaseUser(userDetails);
        user.setPassword(loginUserDetails.getPassword());
        return user;
    }

    public static String getCurrentTenantId() {
        return SecurityUtils.getCurrentUserObject().getTenantId();
    }
}
