package com.huazhu.springbootflowable.config.oauth2;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.stereotype.Component;

/**
 * 认证服务器
 * @author ruanruan
 */
@Component
@Configurable
@EnableAuthorizationServer
public class UserAuthorizationServer  {

}
