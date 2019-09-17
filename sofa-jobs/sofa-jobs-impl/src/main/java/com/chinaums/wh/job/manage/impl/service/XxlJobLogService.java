package com.chinaums.wh.job.manage.impl.service;

import com.chinaums.wh.job.manage.impl.core.model.XxlJobLog;
import me.izhong.dashboard.manage.service.CrudBaseService;

import java.util.Date;
import java.util.List;

public interface XxlJobLogService extends CrudBaseService<Long,XxlJobLog> {
    long triggerCountByHandleCode(int successCode);

    List<Long> findFailJobLogIds();

    long updateAlarmStatus(long failLogId, int oldStatus, int newStatus);

    void clearLog(Long jobGroup, Long jobId, Date clearBeforeTime, int clearBeforeNum);
}