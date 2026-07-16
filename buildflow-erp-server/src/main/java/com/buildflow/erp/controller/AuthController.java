package com.buildflow.erp.controller;

import com.buildflow.erp.common.constants.Constants;
import com.buildflow.erp.common.result.R;
import com.buildflow.erp.common.utils.PasswordUtil;
import com.buildflow.erp.dto.LoginRequest;
import com.buildflow.erp.dto.LoginResponse;
import com.buildflow.erp.dto.MenuVO;
import com.buildflow.erp.entity.SysMenu;
import com.buildflow.erp.entity.SysRole;
import com.buildflow.erp.entity.SysUser;
import com.buildflow.erp.security.JwtTokenUtil;
import com.buildflow.erp.service.SysMenuService;
import com.buildflow.erp.service.SysRoleService;
import com.buildflow.erp.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 提供系统登录和登出接口，负责用户身份验证、账号锁定检查及JWT令牌生成
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登录接口
     * 验证用户名密码，检查账号禁用和锁定状态，登录失败累计错误次数，成功后重置并返回JWT令牌及用户信息
     *
     * @param loginRequest 登录请求参数，包含username和password
     * @return 登录成功返回LoginResponse（含token、用户信息、菜单权限树）；失败返回错误提示
     */
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 1. 根据用户名查询用户
        SysUser user = sysUserService.getByUsername(loginRequest.getUsername());
        if (user == null) {
            return R.fail("用户名或密码错误");
        }

        // 2. 检查账号是否禁用
        if (Constants.USER_STATUS_DISABLED.equals(user.getStatus())) {
            return R.fail("账号已被禁用，请联系管理员");
        }

        // 3. 检查账号是否锁定（错误次数达上限且未过锁定时间）
        if (user.getLoginFailCount() != null
                && user.getLoginFailCount() >= Constants.PASSWORD_MAX_RETRY
                && user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            return R.fail("账号已锁定，请" + Constants.ACCOUNT_LOCK_MINUTES + "分钟后重试");
        }

        // 4. 验证密码
        if (!PasswordUtil.verify(loginRequest.getPassword(), user.getPassword())) {
            // 密码错误，累加失败次数
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
            user.setLoginFailCount(failCount);
            // 达到最大错误次数，锁定账号
            if (failCount >= Constants.PASSWORD_MAX_RETRY) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(Constants.ACCOUNT_LOCK_MINUTES));
            }
            sysUserService.update(user);
            return R.fail("用户名或密码错误");
        }

        // 5. 登录成功，重置错误次数和锁定时间
        user.setLoginFailCount(0);
        user.setLockedUntil(null);
        sysUserService.update(user);

        // 6. 生成JWT令牌
        String token = jwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getRoleId());

        // 7. 构建响应对象
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRoleId(user.getRoleId());

        // 8. 查询角色名称
        R<List<SysRole>> roleResult = sysRoleService.list();
        if (roleResult.getData() != null) {
            roleResult.getData().stream()
                    .filter(role -> role.getId().equals(user.getRoleId()))
                    .findFirst()
                    .ifPresent(role -> response.setRoleName(role.getRoleName()));
        }

        // 9. 查询角色菜单并转换为MenuVO树
        R<List<SysMenu>> menuResult = sysMenuService.getByRoleId(user.getRoleId());
        if (menuResult.getData() != null) {
            response.setMenus(buildMenuTree(menuResult.getData()));
        } else {
            response.setMenus(new ArrayList<>());
        }

        return R.ok(response);
    }

    /**
     * 用户登出接口
     * 前端清除本地存储的token即可，服务端无需额外处理
     *
     * @return 操作成功提示
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        return R.ok("登出成功", null);
    }

    /**
     * 将菜单列表构建为树形结构
     * 根据parentId递归组装父子关系，顶级菜单的parentId为0
     *
     * @param menus 菜单实体列表，平铺结构
     * @return 树形结构的MenuVO列表
     */
    private List<MenuVO> buildMenuTree(List<SysMenu> menus) {
        // 将实体转换为VO
        List<MenuVO> voList = menus.stream().map(menu -> {
            MenuVO vo = new MenuVO();
            vo.setId(menu.getId());
            vo.setParentId(menu.getParentId());
            vo.setMenuName(menu.getMenuName());
            vo.setPath(menu.getPath());
            vo.setIcon(menu.getIcon());
            vo.setSort(menu.getSort());
            return vo;
        }).collect(Collectors.toList());

        // 递归组装树
        return voList.stream()
                .filter(vo -> vo.getParentId() == 0)
                .peek(vo -> vo.setChildren(getChildren(vo.getId(), voList)))
                .collect(Collectors.toList());
    }

    /**
     * 递归获取子菜单列表
     *
     * @param parentId 父级菜单ID
     * @param allMenus 所有菜单VO列表
     * @return 当前父级下的子菜单列表
     */
    private List<MenuVO> getChildren(Long parentId, List<MenuVO> allMenus) {
        return allMenus.stream()
                .filter(vo -> vo.getParentId().equals(parentId))
                .peek(vo -> vo.setChildren(getChildren(vo.getId(), allMenus)))
                .collect(Collectors.toList());
    }
}
