package com.buildflow.erp.security;

import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.entity.SysUser;
import com.buildflow.erp.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户认证服务实现
 * 实现SpringSecurity的UserDetailsService接口，从数据库加载用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    /**
     * 根据用户名加载用户信息
     * @param username 用户名
     * @return UserDetails SpringSecurity用户详情对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (Constants.USER_STATUS_DISABLED.equals(user.getStatus())) {
            throw new UsernameNotFoundException("账号已被禁用: " + username);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRoleId()));

        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
