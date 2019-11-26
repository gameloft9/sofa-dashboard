package me.izhong.jobs.agent.util;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import me.izhong.common.util.CronExpression;
import me.izhong.jobs.agent.service.JobServiceReference;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.model.Job;
import me.izhong.jobs.type.TriggerTypeEnum;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Slf4j
public class TriggerUtil {

    /**
     * 设置任务下次的调度时间
     * @param jobId
     * @param triggerNextTime
     */
    public static void updateJobNextTriggerTime(Long jobId, Date triggerNextTime){
        Assert.notNull(jobId,"");
        Assert.notNull(triggerNextTime,"");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        Job info = facade.findByJobId(jobId);
        if(info == null)
            throw new RuntimeException("任务不存在:" + jobId);
        facade.updateJobNextTriggerTime(jobId,triggerNextTime);
    }

    /**
     * 设置任务从明天开始运行，具体时间还是按照任务的cron来决定
     * @param jobId
     */
    public static void updateJobRunNextDay(Long jobId){
        Assert.notNull(jobId,"");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        Job info = facade.findByJobId(jobId);
        if(info == null)
            throw new RuntimeException("任务不存在:" + jobId);
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR,1);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            Date nextValidTime = new CronExpression(info.getJobCron()).getNextValidTimeAfter(calendar.getTime());
            if (nextValidTime != null) {
                facade.updateJobNextTriggerTime(jobId,nextValidTime);
            }
        } catch (Exception ee) {
            log.error("设置任务下次运行时间异常",ee);
        }
    }

    /**
     * 立即运行一次任务
     * @param jobId
     */
    public static void triggerJob(Long jobId){
        Assert.notNull(jobId,"");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        facade.trigger(jobId,TriggerTypeEnum.SCRIPT,-1, null);
    }

    public static void triggerJob(Long jobId, Map<String,String> params){
        triggerJob(jobId,JSON.toJSONString(params));
    }


    public static void triggerJob(Long jobId, String params){
        Assert.notNull(jobId,"");
        IJobMngFacade facade = ContextUtil.getBean(JobServiceReference.class).getJobMngFacade();
        facade.trigger(jobId,TriggerTypeEnum.SCRIPT,-1, params);
    }

}
