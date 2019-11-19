package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.domain.PageModel;
import me.izhong.domain.PageRequest;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.model.ReturnT;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ZJobInfoService extends CrudBaseService<Long,ZJobInfo> {
    List<ZJobInfo> scheduleJobQuery(long maxNextTime);

    void scheduleUpdate(ZJobInfo jobInfo);

    void updateWaitAgain(Long jobId, Boolean waitAgain);

    void updateRunningTriggers(Long jobId, List<Long> runningTriggerIds);

    void updateJobScriptId(Long jobId, Long scriptId);

    PageModel<ZJobInfo> pageList(PageRequest request, ZJobInfo jobInfo);

    List<ZJobInfo> findRunningJobs();

    /**
     * addJobScript job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> addJob(ZJobInfo jobInfo);

    /**
     * updateJobGroup job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> updateJob(ZJobInfo jobInfo);

    /**
     * removeJobGroup job
     * 	 *
     * @param id
     * @return
     */
    public ReturnT<String> removeJob(long id);

    /**
     * start job
     *
     * @param id
     * @return
     */
    public ReturnT<String> enableJob(long id);

    /**
     * stop job
     *
     * @param id
     * @return
     */
    public ReturnT<String> disableJob(long id);

    /**
     * dashboard info
     *
     * @return
     */
    public Map<String,Object> dashboardInfo();

    /**
     * chart info
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);

}
