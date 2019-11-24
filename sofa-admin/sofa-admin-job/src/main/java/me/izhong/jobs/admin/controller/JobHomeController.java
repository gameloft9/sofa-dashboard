package me.izhong.jobs.admin.controller;

import me.izhong.dashboard.manage.domain.Message;
import me.izhong.dashboard.manage.entity.SysMenu;
import me.izhong.dashboard.manage.security.UserInfoContextHelper;
import me.izhong.dashboard.manage.service.SysMenuService;
import me.izhong.model.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class JobHomeController {


    @Autowired
    private SysMenuService sysMenuService;

    // 系统介绍
    @GetMapping("/system/main_job")
    public String main(ModelMap mmap) {
        mmap.put("version", "1.0");
        return "ext/djob/main_v1";
    }


}