package me.izhong.jobs.manage.impl.service;

import me.izhong.db.common.service.CrudBaseService;
import me.izhong.jobs.manage.impl.core.model.XxlJobLog;

import java.util.Date;
import java.util.List;

public interface XxlJobLogService extends CrudBaseService<Long,XxlJobLog> {
    long triggerCountByHandleCode(int successCode);

    List<Long> findFailJobLogIds();

    XxlJobLog insertTriggerBeginMessage(Long jobId, Long jobGroupId, String jobDesc, Date trggerTime, Integer finalFailRetryCount);

    void updateTriggerDoneMessage(Long jobLogId, String executorAddress,String executorParam,
                                  Integer triggerCode,String triggerMsg);
    void updateHandleStartMessage(Long triggerId, Date startTime);

    long updateAlarmStatus(long failLogId, int oldStatus, int newStatus);

    void updateExecutorAddress(Long jobLogId, String address);

    void clearLog(Long jobId, Date clearBeforeTime, Integer clearBeforeNum);

    void clearLog(Long[] jobLogIds);

}
