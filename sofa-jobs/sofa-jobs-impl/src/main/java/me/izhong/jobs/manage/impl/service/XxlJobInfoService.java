package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.domain.PageModel;
import me.izhong.domain.PageRequest;
import me.izhong.jobs.manage.impl.core.model.XxlJobInfo;
import me.izhong.model.ReturnT;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface XxlJobInfoService extends CrudBaseService<Long,XxlJobInfo> {
    List<XxlJobInfo> scheduleJobQuery(long maxNextTime);

    void scheduleUpdate(XxlJobInfo jobInfo);

    void updateWaitAgain(Long jobId, Boolean waitAgain);

    void updateRunningTriggers(Long jobId, List<Long> runningTriggerIds);

    PageModel<XxlJobInfo> pageList(PageRequest request, XxlJobInfo jobInfo);

    List<XxlJobInfo> findRunningJobs();

    /**
     * addJobScript job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> addJob(XxlJobInfo jobInfo);

    /**
     * updateJobGroup job
     *
     * @param jobInfo
     * @return
     */
    public ReturnT<String> updateJob(XxlJobInfo jobInfo);

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
