package com.cjc.config;

import com.cjc.realm.LoginRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
public class ShiroConfig {

    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher(){
        HashedCredentialsMatcher hash = new HashedCredentialsMatcher();
        hash.setHashAlgorithmName("md5");
        hash.setHashIterations(7);
        hash.setStoredCredentialsHexEncoded(true);
        return hash;
    }

    @Bean
    public LoginRealm loginRealm(){
        LoginRealm loginRealm = new LoginRealm();
        loginRealm.setCredentialsMatcher(hashedCredentialsMatcher());
        return loginRealm;
    }

    @Bean
    public DefaultWebSecurityManager defaultWebSecurityManager(){
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(loginRealm());
        return defaultWebSecurityManager;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(){
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        factoryBean.setSecurityManager(defaultWebSecurityManager());
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("/login/**", DefaultFilter.anon.name());
        map.put("/seller/register", DefaultFilter.anon.name());
        map.put("/seller/queryByUsername", DefaultFilter.anon.name());
        map.put("/admin/register", DefaultFilter.anon.name());
        map.put("/**",DefaultFilter.authc.name());
        factoryBean.setFilterChainDefinitionMap(map);
        return factoryBean;
    }
}
