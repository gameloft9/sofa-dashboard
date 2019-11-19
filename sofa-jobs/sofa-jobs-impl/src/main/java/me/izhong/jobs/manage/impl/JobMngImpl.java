package me.izhong.jobs.manage.impl;

import com.alipay.sofa.runtime.api.annotation.SofaService;
import com.alipay.sofa.runtime.api.annotation.SofaServiceBinding;
import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.exception.BusinessException;
import me.izhong.db.common.service.MongoDistributedLock;
import me.izhong.domain.PageModel;
import me.izhong.domain.PageRequest;
import me.izhong.jobs.manage.IJobMngFacade;
import me.izhong.jobs.manage.impl.core.cron.CronExpression;
import me.izhong.jobs.manage.impl.core.model.XxlJobGroup;
import me.izhong.jobs.manage.impl.core.model.XxlJobInfo;
import me.izhong.jobs.manage.impl.core.model.XxlJobLog;
import me.izhong.jobs.manage.impl.core.model.ZJobStats;
import me.izhong.jobs.manage.impl.core.thread.JobTriggerPoolHelper;
import me.izhong.jobs.manage.impl.core.trigger.TriggerTypeEnum;
import me.izhong.jobs.manage.impl.core.util.JobGroupUtil;
import me.izhong.jobs.manage.impl.core.util.JobInfoUtil;
import me.izhong.jobs.manage.impl.core.util.JobLogUtil;
import me.izhong.jobs.manage.impl.core.util.JobStatsUtil;
import me.izhong.jobs.manage.impl.service.*;
import me.izhong.jobs.model.*;
import me.izhong.model.ReturnT;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@SofaService(interfaceType = IJobMngFacade.class, uniqueId = "${service.unique.id}", bindings = { @SofaServiceBinding(bindingType = "bolt") })
public class JobMngImpl implements IJobMngFacade {

    @Autowired
    private XxlJobRegistryService registryService;

    @Autowired
    private XxlJobInfoService jobInfoService;

    @Autowired
    private XxlJobGroupService jobGroupService;

    @Autowired
    private XxlJobLogService jobLogService;

    @Autowired
    private XxlJobLogGlueService jobLogGlueService;

    @Autowired
    private ZJobStatsService jobStatsService;
    @Autowired
    private ZJobConfigService zJobConfigService;
    @Autowired
    private MongoDistributedLock mongoDistributedLock;

    @Autowired
    private JobAgentServiceReference jobAgentServiceReference;


    @Override
    public PageModel<Job> pageList(PageRequest request, Job ino) {
        XxlJobInfo se = null;
        if(ino != null) {
            se = JobInfoUtil.toDbBean(ino);
        }
        PageModel<XxlJobInfo> gs = jobInfoService.selectPage(request,se);
        if(gs != null && gs.getRows().size() > 0) {
            List<Job> jgs = gs.getRows().stream().map(e -> JobInfoUtil.toRpcBean(e)).map(e->{e.setGlueSource(null);return e;}).collect(Collectors.toList());
            return PageModel.instance(gs.getCount(),jgs);
        }
        return null;
    }

    public List<Job> search(String jobKey, String jobName, String jobGroup) {
        log.info("call search:{}",jobKey);
        return null;
    }

    public long count(String jobKey, String jobName, String jobGroup) {
        return 0;
    }

    @Override
    public Job findByJobId(Long jobId) {
        return JobInfoUtil.toRpcBean(jobInfoService.selectByPId(jobId));
    }

    @Override
    public ReturnT<String> add(Job job) {
        XxlJobInfo jobInfo = new XxlJobInfo();
        BeanUtils.copyProperties(job, jobInfo);
        return jobInfoService.addJob(jobInfo);
    }

    @Override
    public ReturnT<String> remove(Long jobId) {
        return jobInfoService.removeJob(jobId);
    }

    @Override
    public ReturnT<String> update(Job job) {
        XxlJobInfo jobInfo = new XxlJobInfo();
        BeanUtils.copyProperties(job,jobInfo);
        return jobInfoService.updateJob(jobInfo);
    }

    @Override
    public ReturnT<String> enable(Long jobId) {
        return jobInfoService.enableJob(jobId);
    }

    @Override
    public ReturnT<String> disable(Long jobId) {
        return jobInfoService.disableJob(jobId);
    }

    @Override
    public ReturnT<String> kill(Long triggerId) {
        log.info("kill triggerId:{}",triggerId);
        XxlJobLog jobLog = jobLogService.selectByPId(triggerId);
        return jobAgentServiceReference.jobAgentService.kill(jobLog.getJobId(),triggerId);
    }

    @Override
    public ReturnT<String> trigger(Long jobId) {
        JobTriggerPoolHelper.trigger(jobId, TriggerTypeEnum.MANUAL, -1, null);
        return ReturnT.SUCCESS;
    }

    @Override
    public void uploadStatics(LogStatics logStatics) {
        Long triggerId = logStatics.getTriggerId();
        log.info("收到日志job:{} triggerId:{} 内容:{}",logStatics.getJobId(),logStatics.getTriggerId(),logStatics.getLogData());
        //收集agent的日志
        /*XxlJobLog jobLog = jobLogService.selectByPId(triggerId);
        if(jobLog != null) {
            String data = logStatics.getLogData();
            if(StringUtils.isNotBlank(data)) {
                String oi = jobLog.getHandleMsg();
                if(oi == null){
                    oi = "";
                }
                data = oi + data + "<br/>";
                if(data.length() < 1000*1000) {
                    jobLog.setHandleMsg(data);
                    jobLogService.update(jobLog);
                }
            }
        } else {
            log.error("jobLog未找到 triggerId:{}",triggerId);
        }*/
    }

    @Override
    public void uploadJobStartStatics(Long triggerId, Date startTime) {
        log.info("收到Job执行开始信息 triggerId:{}",triggerId);
        if(triggerId == null) {
            throw BusinessException.build("上送执行信息的triggerId为空");
        }
        jobLogService.updateHandleStartMessage(triggerId,startTime);
    }

    @Override
    public void uploadJobEndStatics(Long triggerId, Date endTime, Integer resultStatus, String message) {
        log.info("收到Job执行结束信息 triggerId:{} resultStatus:{}  message:{}",triggerId,resultStatus,message);
        //收集agent的日志
        XxlJobLog jobLog = jobLogService.selectByPId(triggerId);
        if(jobLog != null) {
            jobLog.setFinishHandleTime(endTime);
            Date startTime = jobLog.getHandleTime();
            if(startTime != null){
                long second1 = DateUtils.getFragmentInMilliseconds(startTime,Calendar.YEAR);
                long second2 = DateUtils.getFragmentInMilliseconds(endTime,Calendar.YEAR);
                String dur = DurationFormatUtils.formatPeriod(second1,second2,"HH:mm:ss");
                jobLog.setCostHandleTime(dur);
            }
            jobLog.setHandleCode(resultStatus);
            jobLog.setHandleMsg(message + (jobLog.getHandleMsg() == null ? "" : ":" + jobLog.getHandleMsg()));
            jobLogService.update(jobLog);

            triggerContinue(jobLog.getJobId());

        }
    }

    private void triggerContinue(Long jobId) {
        XxlJobInfo jobInfo = jobInfoService.selectByPId(jobId);
        if(Boolean.TRUE.equals(jobInfo.getWakeAgain())) {
            log.info("重新唤起任务的执行");
            jobInfo.setWakeAgain(Boolean.FALSE);
            try {
                Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date(jobInfo.getTriggerNextTime()));
                if (nextValidTime != null) {
                    jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
                    jobInfo.setTriggerNextTime(nextValidTime.getTime());
                } else {
                    jobInfo.setTriggerStatus(0L);
                    jobInfo.setTriggerLastTime(0L);
                    jobInfo.setTriggerNextTime(0L);
                }
            } catch (Exception ee) {
                log.error("",ee);
            }
            jobInfoService.updateJob(jobInfo);
            JobTriggerPoolHelper.trigger(jobInfo.getJobId(), TriggerTypeEnum.CONTINUE, -1,  null);
            log.info("WakeAgain触发任务执行 jobId:{}" ,jobInfo.getId() );
        }
    }

    @Override
    public void uploadJobErrorStatics(Long triggerId, Date endTime, Integer resultStatus, String message) {
        log.info("收到Job异常结束信息 triggerId:{} resultStatus:{}  message:{}",triggerId,resultStatus,message);
        //收集agent的日志
        XxlJobLog jobLog = jobLogService.selectByPId(triggerId);
        if(jobLog != null) {
            if(jobLog.getHandleCode() != null) {
                log.info("job已经执行结束忽略kill消息");
                return;
            }
            jobLog.setFinishHandleTime(endTime);
            Date startTime = jobLog.getHandleTime();
            if(startTime != null){
                long second1 = DateUtils.getFragmentInMilliseconds(startTime,Calendar.YEAR);
                long second2 = DateUtils.getFragmentInMilliseconds(endTime,Calendar.YEAR);
                String dur = DurationFormatUtils.formatPeriod(second1,second2,"HH:mm:ss");
                jobLog.setCostHandleTime(dur);
            }
            jobLog.setHandleCode(resultStatus);
            jobLog.setHandleMsg(message + (jobLog.getHandleMsg() == null ? "" : ":" + jobLog.getHandleMsg()));
            jobLogService.update(jobLog);

            triggerContinue(jobLog.getJobId());

        }
    }


    @Override
    public PageModel<JobLog> logPageList(PageRequest request, JobLog ino) {
        XxlJobLog se = null;
        if(ino != null) {
            se = JobLogUtil.toDbBean(ino);
        }
        PageModel<XxlJobLog> gs = jobLogService.selectPage(request,se);
        if(gs != null && gs.getRows().size() > 0) {
            List<JobLog> jgs = gs.getRows().stream().map(e -> JobLogUtil.toRpcBean(e)).collect(Collectors.toList());
            return PageModel.instance(gs.getCount(),jgs);
        }
        return null;
    }

    @Override
    public JobLog findJobLogByJobLogId(Long jobLogId) {
        return JobLogUtil.toRpcBean(jobLogService.selectByPId(jobLogId));
    }

    @Override
    public void clearLog(Long jobId, Date clearBeforeTime, Integer clearBeforeNum) {
        log.info("清理日志 jobId:{} clearBeforeTime:{} clearBeforeNum:{}",jobId,clearBeforeTime,clearBeforeNum);
        jobLogService.clearLog(jobId,clearBeforeTime,clearBeforeNum);
    }

    @Override
    public void clearLog(Long[] jobLogIds){
        log.info("清理日志 jobLogIds:{}",jobLogIds);
        jobLogService.clearLog(jobLogIds);
    }

    @Override
    public LogResult catLog(Long jobId, Long logId,long triggerTime,  int fromLineNum) {
        return jobAgentServiceReference.jobAgentService.catLog(jobId,logId,triggerTime,fromLineNum);
    }

    @Override
    public void update(JobLog jobLog) {

    }

    @Override
    public List<JobScript> findJobScriptByJobId(Long jobId) {
        return null;
    }

    @Override
    public JobScript findByJobScriptId(String scriptId) {
        return null;
    }

    @Override
    public void addJobScript(JobScript script) {

    }

    @Override
    public void removeOldLog(Long jobId, int keeyDays) {

    }

    /**
     * group
     * @return
     */
    @Override
    public List<JobGroup> selectAllJobGroup() {
        List<XxlJobGroup> gs = jobGroupService.selectAll();
        if(gs != null && gs.size() > 0) {
            return gs.stream().map(e-> JobGroupUtil.toRpcBean(e)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public PageModel<JobGroup> selectJobGroupPage(PageRequest request, JobGroup ino) {
        XxlJobGroup se = null;
        if(ino !=null) {
            se = JobGroupUtil.toDbBean(ino);
        }
        PageModel<XxlJobGroup> gs = jobGroupService.selectPage(request,se);
        if(gs != null && gs.getRows().size() > 0) {
            List<JobGroup> jgs = gs.getRows().stream().map(e -> JobGroupUtil.toRpcBean(e)).collect(Collectors.toList());
            return PageModel.instance(gs.getCount(),jgs);
        }
        return null;
    }

    @Override
    public JobGroup findJobGroup(Long groupId) {
        return JobGroupUtil.toRpcBean(jobGroupService.selectByPId(groupId));
    }

    @Override
    public JobGroup addJobGroup(JobGroup group) {
        XxlJobGroup db = jobGroupService.insert(JobGroupUtil.toDbBean(group));
        return JobGroupUtil.toRpcBean(db);
    }

    @Override
    public JobGroup updateJobGroup(JobGroup group) {
        XxlJobGroup db = jobGroupService.update(JobGroupUtil.toDbBean(group));
        return JobGroupUtil.toRpcBean(db);
    }

    @Transactional
    @Override
    public long removeJobGroup(List<Long> groupIds) {
        long c = 0;
        for(Long l:groupIds){
            c += jobGroupService.deleteByPId(l);
        }
        return c;
    }

    @Override
    public PageModel<JobStats> selectJobStatsPage(PageRequest request, JobStats stats) {
        ZJobStats se = null;
        if(stats !=null) {
            se = JobStatsUtil.toDbBean(stats);
        }
        PageModel<ZJobStats> gs = jobStatsService.selectPage(request,se);
        if(gs != null && gs.getRows().size() > 0) {
            List<JobStats> jgs = gs.getRows().stream().map(e -> {
                return JobStatsUtil.toRpcBean(e);
            }).collect(Collectors.toList());
            return PageModel.instance(gs.getCount(),jgs);
        }
        return null;
    }

    @Override
    public JobStats findJobStatsByKey(String key) {
        Assert.notNull(key,"");
        ZJobStats zs = jobStatsService.findByKey(key);
        return JobStatsUtil.toRpcBean(zs);
    }

    @Override
    public List<JobStats> findByType(String type) {
        Assert.notNull(type,"");
        List<ZJobStats> zs = jobStatsService.findByType(type);
        if(zs == null || zs.size() == 0)
            return null;
        List<JobStats> jgs = zs.stream().map(e -> JobStatsUtil.toRpcBean(e)).collect(Collectors.toList());
        return jgs;
    }

    @Override
    public JobStats insertOrUpdateJobStats(JobStats stats) {
        Assert.notNull(stats,"");
        ZJobStats zJobStats = JobStatsUtil.toDbBean(stats);
        ZJobStats db = jobStatsService.insertOrUpdate(zJobStats);
        JobStats rpc =  JobStatsUtil.toRpcBean(db);
        return rpc;
    }

    @Override
    public boolean deleteJobStats(String key) {
        return jobStatsService.removeStats(key);
    }

    @Override
    public boolean lockKey(String key, long milliseconds) {
        return mongoDistributedLock.getLock(key,milliseconds);
    }

    @Override
    public void releaseKey(String key) {
        mongoDistributedLock.releaseLock(key);
    }

    @Override
    public String findConfigByKey(String configKey) {
        return zJobConfigService.selectConfigByKey(configKey);
    }

}
