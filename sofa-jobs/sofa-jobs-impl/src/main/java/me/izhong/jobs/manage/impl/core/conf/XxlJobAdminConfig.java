package me.izhong.jobs.manage.impl.core.conf;

import lombok.Getter;
import lombok.Setter;
import me.izhong.jobs.manage.impl.service.ZJobGroupService;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Getter
@Setter
public class XxlJobAdminConfig implements InitializingBean {
    private static XxlJobAdminConfig adminConfig = null;
    public static XxlJobAdminConfig getAdminConfig() {
        return adminConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        adminConfig = this;
    }
    @Resource
    private ZJobLogService zJobLogService;
    @Resource
    private ZJobInfoService zJobInfoService;
    @Resource
    private ZJobGroupService xxlJobGroupService;

}
