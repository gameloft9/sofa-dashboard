package me.izhong.jobs.agent.util;

import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.type.TriggerTypeEnum;
import org.springframework.util.Assert;

import java.util.Date;

public class TriggerUtil {

    public static void updateJobNextTriggerTime(Long jobId, Date triggerNextTime){
        Assert.notNull(jobId,"");
        Assert.notNull(triggerNextTime,"");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        Job info = facade.findByJobId(jobId);
        if(info == null)
            throw new RuntimeException("任务不存在:" + jobId);
        facade.updateJobNextTriggerTime(jobId,triggerNextTime);
    }

    public static void triggerJob(Long jobId){
        Assert.notNull(jobId,"");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        facade.trigger(jobId,TriggerTypeEnum.SCRIPT);
    }

}
