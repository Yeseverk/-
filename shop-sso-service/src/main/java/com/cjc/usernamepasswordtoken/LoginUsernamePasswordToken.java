package com.cjc.usernamepasswordtoken;

import com.cjc.constants.LoginConstants;
import org.apache.shiro.authc.UsernamePasswordToken;

public class LoginUsernamePasswordToken extends UsernamePasswordToken {

    private LoginConstants type;

    public LoginUsernamePasswordToken(String username, String password, LoginConstants type) {
        super(username, password);
        this.type = type;
    }

    public LoginUsernamePasswordToken() {
    }

    public LoginConstants getType() {
        return type;
    }

    public void setType(LoginConstants type) {
        this.type = type;
    }
}
