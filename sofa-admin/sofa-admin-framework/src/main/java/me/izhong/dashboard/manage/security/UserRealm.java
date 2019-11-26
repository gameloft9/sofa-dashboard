package me.izhong.dashboard.manage.security;

import me.izhong.db.common.exception.BusinessException;
import me.izhong.model.UserInfo;
import me.izhong.dashboard.manage.constants.Global;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.dashboard.manage.entity.SysUser;
import me.izhong.dashboard.manage.expection.*;
import me.izhong.dashboard.manage.expection.user.UserBlockedException;
import me.izhong.dashboard.manage.expection.user.UserNotFoundException;
import me.izhong.dashboard.manage.expection.user.UserPasswordNotMatchException;
import me.izhong.dashboard.manage.expection.user.UserPasswordRetryLimitExceedException;
import me.izhong.dashboard.manage.security.service.LoginService;
import me.izhong.dashboard.manage.service.SysDeptService;
import me.izhong.dashboard.manage.service.SysMenuService;
import me.izhong.dashboard.manage.service.SysRoleService;
import me.izhong.dashboard.manage.util.ServletUtil;
import me.izhong.dashboard.manage.util.UserConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 自定义Realm 处理登录 权限
 */
public class UserRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(UserRealm.class);

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private SysDeptService sysDeptService;


    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        UserInfo user = UserInfoContextHelper.getLoginUser();
        // 角色列表
        Set<String> roles = new HashSet<String>();
        // 菜单，按钮权限列表
        Set<String> menus = new HashSet<String>();
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 管理员拥有所有权限
//        if (user.isAdmin()) {
//            info.addRole("admin");
//            info.addStringPermission("*:*:*");
//        } else {
        roles = sysRoleService.selectRoleKeys(user.getUserId());
        menus = sysMenuService.selectPermsByUserId(user.getUserId());
        // 角色加入AuthorizationInfo认证对象
        info.setRoles(roles);
        // 权限加入AuthorizationInfo认证对象
        info.setStringPermissions(menus);

        return info;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        String username = upToken.getUsername();
        String password = "";
        if (upToken.getPassword() != null) {
            password = new String(upToken.getPassword());
        }

        SysUser user = null;
        try {
            user = loginService.login(username, password);
        } catch (CaptchaException e) {
            throw new AuthenticationException(e.getMessage(), e);
        } catch (UserNotFoundException e) {
            throw new UnknownAccountException(e.getMessage(), e);
        } catch (UserPasswordNotMatchException e) {
            throw new IncorrectCredentialsException(e.getMessage(), e);
        } catch (UserPasswordRetryLimitExceedException e) {
            throw new ExcessiveAttemptsException(e.getMessage(), e);
        } catch (UserBlockedException e) {
            throw new LockedAccountException(e.getMessage(), e);
        } catch (RoleBlockedException e) {
            throw new LockedAccountException(e.getMessage(), e);
        } catch (Exception e) {
            log.info("对用户[" + username + "]进行登录验证..验证未通过{}", e.getMessage());
            throw new AuthenticationException(e.getMessage(), e);
        }
        UserInfo loginUser = UserConvertUtil.convert(user);

        String conextPath = ServletUtil.getRequest().getContextPath();
        String avatarUrl = conextPath + Global.getAvatarMapping();
        if(StringUtils.isNotBlank(loginUser.getAvatar()) && !loginUser.getAvatar().startsWith("http")) {
            loginUser.setAvatar(avatarUrl + user.getAvatar());
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(loginUser, password, getName());

        //根据roles 计算数据权限
        List<SysRole> rs = sysRoleService.selectRolesByUserId(loginUser.getUserId());
        // 实体名称 -> deptIds
        Long deptId = loginUser.getDeptId();
        if(deptId == null)
            throw BusinessException.build("用户["+loginUser+"]缺少所属部门记录，不能登陆");

        SysDept sysDept = sysDeptService.selectDeptByDeptId(deptId);
        List<Long> desces = sysDept.getDescendents();
        List<SysDept> allDepts = sysDeptService.selectAll();
        List<Long> allDeptIds = allDepts.stream().map(e->e.getDeptId()).collect(Collectors.toList());

        allDepts.forEach(e->{
            UserInfo.getDeptIdNames().put(e.getDeptId(),e.getDeptName());
        });

        //遍历用户角色
        rs.forEach(e -> {
            Long roleId = e.getRoleId();
            String scope = e.getDataScope();

            List<String> rolePerms = sysMenuService.selectPermsByRoleId(roleId);
            List<Long> roleDeptIds = sysRoleService.selectDeptIdsByRoleId(roleId);
            if (rolePerms != null && rolePerms.size() > 0) {
                rolePerms.forEach(rpk -> {
                    if (StringUtils.equals("1", scope)) {
                        loginUser.setHasAllDeptPerm(true);
                        loginUser.addScopeData(rpk, allDeptIds);
                    } else if (StringUtils.equals("2", scope)) {
                        loginUser.addScopeData(rpk, roleDeptIds);
                    } else if (StringUtils.equals("3", scope)) {
                        loginUser.addScopeData(rpk, deptId);
                    } else if (StringUtils.equals("4", scope)) {
                        desces.add(deptId);
                        loginUser.addScopeData(rpk, desces);
                    } else {
                        throw BusinessException.build(e.getRoleName() + "的datascope不复合规则");
                    }
                });
            }
        });

        return info;
    }

    /**
     * 清理缓存权限
     */
    public void clearCachedAuthorizationInfo() {
        this.clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }
}
