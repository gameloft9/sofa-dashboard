package me.izhong.jobs.manage;

import me.izhong.domain.PageModel;
import me.izhong.domain.PageRequest;
import me.izhong.jobs.model.*;
import me.izhong.model.ReturnT;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.List;

public interface IJobMngFacade {

    PageModel<Job> pageList(PageRequest request, Job ino);

    List<Job> search(String jobKey, String jobName, String jobGroup);

    long count(String jobKey, String jobName, String jobGroup);

    Job findByJobId(Long jobId);

    ReturnT<String> add(Job job);

    ReturnT<String> remove(Long jobId);

    ReturnT<String> update(Job job);

    ReturnT<String> enable(Long jobId);

    ReturnT<String> disable(Long jobId);

    ReturnT<String> kill(Long jobId);

    ReturnT<String> trigger(Long jobId);

    /**
     *  agent注册自己的地址到调度器
     * @param registryParam
     * @return
     */
    ReturnT<String> registryAgent(RegistryParam registryParam);

    /**
     * agent上送执行日志，结果到调度器
     * @param
     * @return
     */
    void uploadStatics(LogStatics logStatics);
    void uploadJobStartStatics(Long triggerId, Date startTime);
    void uploadJobEndStatics(Long triggerId, Date endTime, Integer resultStatus, String message);

    /**
     * 日志相关
     * @param request
     * @param ino
     * @return
     */
    PageModel<JobLog> logPageList(PageRequest request, JobLog ino);

    JobLog findJobLogByJobLogId(Long jobLogId);

    void clearLog(Long jobId, Date clearBeforeTime, Integer clearBeforeNum);
    void clearLog(Long[] jobLogIds);

    LogResult catLog(Long jobId, Long logId, long triggerTime,int fromLineNum);

    void update(JobLog jobLog);

    /**
     * script
     * @param jobId
     * @return
     */
    List<JobScript> findJobScriptByJobId(Long jobId);

    JobScript findByJobScriptId(String scriptId);

    void addJobScript(JobScript script);

    void removeOldLog(Long jobId, int keeyDays);


    /**
     * group
     * @return
     */
    List<JobGroup> selectAllJobGroup();

    PageModel<JobGroup> selectJobGroupPage(PageRequest request, JobGroup group);

    JobGroup findJobGroup(Long groupId);

    JobGroup addJobGroup(JobGroup group);

    JobGroup updateJobGroup(JobGroup group);

    long removeJobGroup(List<Long> groupId);

    //处理任务相关的记录信息，比如执行耗时，是否处理过
    PageModel<JobStats> selectJobStatsPage(PageRequest request, JobStats stats);
    JobStats findJobStatsByKey(String key);
    List<JobStats> findByType(String type);
    JobStats insertOrUpdateJobStats(JobStats stats);
    boolean deleteJobStats(String key);


    boolean lockKey(String key, long milliseconds);
    void releaseKey(String key);

    //查一些系统的参数
    String findConfigByKey(String key);

}
