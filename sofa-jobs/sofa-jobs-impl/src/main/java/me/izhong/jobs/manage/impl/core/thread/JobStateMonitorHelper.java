package me.izhong.jobs.manage.impl.core.thread;

import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.service.MongoDistributedLock;
import me.izhong.jobs.manage.impl.JobAgentServiceReference;
import me.izhong.jobs.manage.impl.core.model.XxlJobInfo;
import me.izhong.jobs.manage.impl.core.model.XxlJobLog;
import me.izhong.jobs.manage.impl.service.XxlJobInfoService;
import me.izhong.jobs.manage.impl.service.XxlJobLogService;
import me.izhong.model.ReturnT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JobStateMonitorHelper {

    // ---------------------- monitor ----------------------
    private Thread monitorThread;
    private volatile boolean toStop = false;
    public static final String LOCK_KEY = "job_state_monitor";

    @Autowired
    private XxlJobInfoService jobInfoService;

    @Autowired
    private XxlJobLogService jobLogService;

    @Autowired
    private JobAgentServiceReference jobAgentServiceReference;
    @Autowired
    private MongoDistributedLock dLock;

    @PostConstruct
    public void start() {
        monitorThread = new Thread(new Runnable() {

            @Override
            public void run() {

                while (!toStop) {
                    boolean lock = false;
                    try {
                        lock = dLock.getLock(LOCK_KEY, 8000);
                        List<XxlJobLog> jobLogs = jobLogService.findRunningJobs();
                        if (jobLogs != null && !jobLogs.isEmpty()) {
                            for (XxlJobLog jobLog : jobLogs) {
                                ReturnT<String> rtStatus = jobAgentServiceReference.jobAgentService.status(jobLog.getJobId(), jobLog.getJobLogId());
                                if(rtStatus.getCode() == ReturnT.SUCCESS_CODE) {
                                    int code = rtStatus.getCode();
                                    String content = rtStatus.getContent();
                                    String message = rtStatus.getMsg();
                                    log.info("检测任务，收到状态 jobId:{}jobDesc:{} jobLogId:{} code:{} message:{}", jobLog.getJobId(), jobLog.getJobDesc(), jobLog.getJobLogId(), code, message);

                                    //任务已经结束
                                    if (StringUtils.equals(content,"DONE")) {
                                        jobLogService.updateHandleDoneMessage(jobLog.getJobLogId(),  405, "异常结束(stats进程触发)");
                                        XxlJobInfo jobInfo = jobInfoService.selectByPId(jobLog.getJobId());
                                        if (jobInfo.getRunningTriggerIds() != null && jobInfo.getRunningTriggerIds().contains(jobLog.getJobLogId())) {
                                            jobInfo.getRunningTriggerIds().remove(jobLog.getJobLogId());
//                                            jobInfo.setRunningCount(jobInfo.getRunningTriggerIds().size());
                                            log.info("移除已经停止任务{},当前还有任务{}", jobLog.getJobLogId(), jobInfo.getRunningTriggerIds().size());
                                            jobInfoService.update(jobInfo);
                                        } else {
                                            log.info("当前运行 {} 队列不包含异常任务（任务进行中）:triggerId:{}", jobInfo.getJobDesc(),jobLog.getJobLogId());
                                        }
                                    }
                                }
                            }
                        }

                        List<XxlJobInfo> jobInfos = jobInfoService.findRunningJobs();
                        for(XxlJobInfo jobInfo : jobInfos ){
                            List<Long> triggerIds = jobInfo.getRunningTriggerIds();
                            boolean needUpdate = false;
                            if(triggerIds != null && triggerIds.size() > 0) {
                                Iterator<Long> it = triggerIds.iterator();
                                while (it.hasNext()) {
                                    Long tgid = it.next();
                                    XxlJobLog jl = jobLogService.selectByPId(tgid);
                                    if(jl == null || jl.getHandleCode() != null) {
                                        //已经结束
                                        it.remove();
                                        needUpdate = true;
                                    }
                                }
                            }
                            if(needUpdate) {
                                log.info("更新任务{}正在运行的trigger:{}",jobInfo.getJobDesc(),triggerIds);
                                jobInfoService.updateRunningTriggers(jobInfo.getJobId(),triggerIds.size(),triggerIds);
                            }
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            log.error("job fail monitor thread error:{}", e);
                        }
                    } finally {
                        if(lock)
                            dLock.releaseLock(LOCK_KEY);
                    }
                    try {
                        TimeUnit.SECONDS.sleep(60);
                    } catch (InterruptedException e) {

                    }
                }
                log.info("job fail monitor thread stop");
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("JobStateMonitorHelper");
        monitorThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

}
