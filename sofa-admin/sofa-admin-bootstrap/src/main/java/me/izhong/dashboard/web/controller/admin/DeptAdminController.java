package me.izhong.dashboard.web.controller.admin;

import me.izhong.db.common.annotation.AjaxWrapper;
import me.izhong.dashboard.manage.annotation.Log;
import me.izhong.dashboard.manage.constants.BusinessType;
import me.izhong.dashboard.manage.entity.SysDept;
import me.izhong.dashboard.manage.entity.SysRole;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.security.config.PermissionConstants;
import me.izhong.dashboard.manage.service.SysDeptService;
import me.izhong.dashboard.manage.domain.Ztree;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


@Controller
@RequestMapping("/system/dept")
public class DeptAdminController {
    private String prefix = "system/dept";

    @Autowired
    private SysDeptService sysDeptService;

    @RequiresPermissions(PermissionConstants.Dept.VIEW)
    @GetMapping()
    public String dept() {
        return prefix + "/dept";
    }

    @RequiresPermissions(PermissionConstants.Dept.VIEW)
    @PostMapping("/list")
    @ResponseBody
    public List<SysDept> list(SysDept sysDept) {
        List<SysDept> sysDeptList = sysDeptService.selectList(sysDept);
        return sysDeptList;
    }

    @RequiresPermissions(PermissionConstants.Dept.ADD)
    @GetMapping("/add/{parentId}")
    public String add(@PathVariable("parentId") Long parentId, ModelMap mmap) {
        SysDept sysDept = null;
        if (parentId == null || parentId.longValue() == 0 || parentId.longValue() == 100) {
            sysDept = new SysDept();
            sysDept.setDeptName("总部");
            sysDept.setDeptId(0L);
        } else {
            sysDept = sysDeptService.selectDeptByDeptId(parentId);
        }
        if (sysDept == null) {
            throw BusinessException.build("部门不存在");
        }
        mmap.put("dept", sysDept);
        return prefix + "/add";
    }

    @Log(title = "部门管理", businessType = BusinessType.ADD)
    @RequiresPermissions(PermissionConstants.Dept.ADD)
    @PostMapping("/add")
    @AjaxWrapper
    public int addSave(SysDept sysDept) {
        if (!sysDeptService.checkDeptNameUnique(sysDept)) {
            throw BusinessException.build("新增部门'" + sysDept.getDeptName() + "'失败，部门名称已存在");
        }
        sysDept.setCreateBy(UserInfoContextHelper.getCurrentLoginName());
        sysDept.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysDeptService.insertDept(sysDept);
    }

    @RequiresPermissions(PermissionConstants.Dept.EDIT)
    @GetMapping("/edit/{deptId}")
    public String edit(@PathVariable("deptId") Long deptId, ModelMap mmap) {
        SysDept sysDept = sysDeptService.selectDeptByDeptId(deptId);
        if (sysDept != null) {
            if (deptId == 0L || sysDept.getParentId() == 0)
                sysDept.setParentName("无");
            else {
                SysDept parentSysDept = sysDeptService.selectDeptByDeptId(sysDept.getParentId());
                sysDept.setParentName(parentSysDept.getDeptName());
            }
        }
        mmap.put("dept", sysDept);
        return prefix + "/edit";
    }


    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions(PermissionConstants.Dept.EDIT)
    @PostMapping("/edit")
    @AjaxWrapper
    public int editSave(SysDept sysDept) {
        if (!sysDeptService.checkDeptNameUnique(sysDept)) {
            throw BusinessException.build("修改部门'" + sysDept.getDeptName() + "'失败，部门名称已存在");
        }

        if(sysDept.getParentId().equals(sysDept.getDeptId())) {
            throw BusinessException.build("修改部门'" + sysDept.getDeptName() + "'失败，上级部门不能是自己");
        }

        sysDept.setUpdateBy(UserInfoContextHelper.getCurrentLoginName());
        return sysDeptService.updateDept(sysDept);
    }

    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @RequiresPermissions(PermissionConstants.Dept.REMOVE)
    @GetMapping("/remove/{deptId}")
    @AjaxWrapper
    public long remove(@PathVariable("deptId") Long deptId) {
        sysDeptService.checkExistChildDept(deptId);
        sysDeptService.checkDeptExistUser(deptId);
        return sysDeptService.remove(deptId);
    }

    @PostMapping("/checkDeptNameUnique")
    @ResponseBody
    public boolean checkDeptNameUnique(SysDept sysDept) {
        return sysDeptService.checkDeptNameUnique(sysDept);
    }

    /**
     * 选择部门树,部门菜单上级部门选择
     */
    @GetMapping("/selectDeptTree")
    public String selectDeptTree(Long deptId, String viewPerm, ModelMap mmap) {
        SysDept sysDept = sysDeptService.selectDeptByDeptId(deptId);
        if (sysDept == null)
            sysDept = sysDeptService.selectDeptByDeptId(1L);
        mmap.put("sysDept", sysDept);
        mmap.put("viewPerm", viewPerm);
        return prefix + "/tree";
    }

    /**
     * 加载部门列表树 所有的
     */
    @GetMapping("/treeData")
    @ResponseBody
    public List<Ztree> treeData() {
        List<Ztree> ztrees = sysDeptService.selectDeptTree(null);
        return ztrees;
    }

    /**
     * 加载部门列表树 当前用户有权限的部门树列表
     */
    @RequiresAuthentication
    @GetMapping("/myTreeData")
    @ResponseBody
    public List<Ztree> myTreeData(String viewPerm) {
        Long userId = UserInfoContextHelper.getCurrentUserId();
        if(StringUtils.isNotBlank(viewPerm)) {
            Set<Long> scop = UserInfoContextHelper.getLoginUser().getScopeData(viewPerm);
            return sysDeptService.selectDeptTreeData(scop.toArray(new Long[]{}));
        }
        return sysDeptService.selectDeptTree(null);
    }

    /**
     * 加载角色部门（数据权限）列表树，某个角色拥有的部门
     */
    @GetMapping("/roleDeptTreeData")
    @ResponseBody
    public List<Ztree> deptTreeData(SysRole sysRole) {
        List<Ztree> ztrees = sysDeptService.roleDeptTreeData(sysRole.getRoleId());
        return ztrees;
    }
}
