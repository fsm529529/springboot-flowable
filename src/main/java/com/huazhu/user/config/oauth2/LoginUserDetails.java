package com.huazhu.user.config.oauth2;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Map;

/**
 * 用户登录信息存储类
 * @author ruanruan
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LoginUserDetails extends User {


    private static final long serialVersionUID = 8617955857505223539L;
    private Map<String,Object> userDetails;

    public LoginUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }
}
