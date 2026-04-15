package com.cjc.realm;

import com.cjc.api.AdminApi;
import com.cjc.api.SellerApi;
import com.cjc.api.UserApi;
import com.cjc.constants.LoginConstants;
import com.cjc.pojo.TbAdmin;
import com.cjc.pojo.TbSeller;
import com.cjc.pojo.TbUser;
import com.cjc.usernamepasswordtoken.LoginUsernamePasswordToken;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

public class LoginRealm extends AuthorizingRealm {

    @Autowired
    private AdminApi adminApi;

    @Autowired
    private SellerApi sellerApi;

    @Autowired
    private UserApi userApi;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        // 获取用户名
        LoginUsernamePasswordToken usernamePasswordToken = (LoginUsernamePasswordToken) authenticationToken;
        String username = usernamePasswordToken.getUsername();
        LoginConstants type = usernamePasswordToken.getType();

        // 判断当前是seller 或者是 admin 或者是 user
        if(type==LoginConstants.ADMIN){
            TbAdmin tbAdmin = adminApi.queryByUsername(username);
            if (tbAdmin == null) {
                return null;
            }
            SimpleAuthenticationInfo authenticationInfo =
                    new SimpleAuthenticationInfo(tbAdmin, tbAdmin.getPasssword(), ByteSource.Util.bytes(tbAdmin.getSalt()),"loginRealm");
            return authenticationInfo;
        } else if(type==LoginConstants.SELLER){
            // seller
            TbSeller seller = sellerApi.queryByUsername(username);
            if (seller == null) {
                return null;
            }
            SimpleAuthenticationInfo authenticationInfo =
                    new SimpleAuthenticationInfo(seller, seller.getPassword(), ByteSource.Util.bytes(seller.getSalt()),"loginRealm");
            return authenticationInfo;
        } else {
            // user
            TbUser user = userApi.queryByUsername(username);
            if (user == null) {
                return null;
            }
            SimpleAuthenticationInfo authenticationInfo =
                    new SimpleAuthenticationInfo(user, user.getPassword(), ByteSource.Util.bytes(user.getSalt()),"loginRealm");
            return authenticationInfo;
        }

    }



    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }
}
