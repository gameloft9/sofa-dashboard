package me.izhong.jobs.manage.impl.service.impl;

import me.izhong.db.common.service.CrudBaseServiceImpl;
import com.mongodb.client.result.UpdateResult;
import me.izhong.jobs.manage.impl.core.model.XxlJobLog;
import me.izhong.jobs.manage.impl.core.model.XxlJobRegistry;
import me.izhong.jobs.manage.impl.service.XxlJobLogService;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class XxlJobLogServiceImpl extends CrudBaseServiceImpl<Long,XxlJobLog> implements XxlJobLogService {
    @Override
    public long triggerCountByHandleCode(int successCode) {
        Query query = new Query();
        query.addCriteria(Criteria.where("triggerCode").is(successCode));
        return super.count(query,null,null);
    }

    @Override
    public List<Long> findFailJobLogIds() {
        Query query = new Query();
        Criteria c1 = Criteria.where("triggerCode").in(0,200).andOperator( Criteria.where("handleCode").is(0)  );
        Criteria c2 = Criteria.where("handleCode").is(200);
        Criteria dest = c1.orOperator(c2).not();
        query.addCriteria(dest);
        List<XxlJobLog> ls = super.selectList(query,null,null);
        if(ls ==null || ls.size() == 0)
            return new ArrayList<>();
        return ls.stream().map(e->e.getJobId()).collect(Collectors.toList());

    }

    @Transactional
    @Override
    public XxlJobLog insertTriggerBeginMessage(Long jobId, Long jobGroupId, String jobDesc, Date triggerTime, Integer finalFailRetryCount) {
        XxlJobLog jobLog = new XxlJobLog();
        jobLog.setJobId(jobId);
        jobLog.setJobGroupId(jobGroupId);
        jobLog.setJobDesc(jobDesc);
        jobLog.setTriggerTime(triggerTime);
        jobLog.setExecutorFailRetryCount(finalFailRetryCount);
        return super.insert(jobLog);
    }

    @Transactional
    @Override
    public void updateTriggerDoneMessage(Long jobLogId, String executorAddress, String executorHandler, 
                                         String executorParam, Integer triggerCode, String triggerMsg) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        Update update = new Update();
        update.set("executorAddress",executorAddress);
        update.set("executorHandler",executorHandler);
        update.set("executorParam",executorParam);
        update.set("triggerCode",triggerCode);
        update.set("triggerMsg",triggerMsg);
        XxlJobLog ur = mongoTemplate.findAndModify(query, update, options, XxlJobLog.class);
    }

    @Override
    public long updateAlarmStatus(long failLogId, int oldStatus, int newStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(failLogId));
        query.addCriteria(Criteria.where("alarmStatus").is(oldStatus));

        Update update = new Update();
        update.set("alarmStatus",newStatus);
        UpdateResult ur = mongoTemplate.updateMulti(query, update, XxlJobLog.class);
        return ur.getModifiedCount();
    }

    @Override
    public void clearLog(Long jobId, Date clearBeforeTime, Integer clearBeforeNum) {
        Query query = new Query();
        if(jobId != null)
            query.addCriteria(Criteria.where("jobId").is(jobId));
        if(clearBeforeTime !=null)
            query.addCriteria(Criteria.where("createTime").lte(clearBeforeTime));
        if(clearBeforeNum > 0)
            query.addCriteria(Criteria.where("jobId").lte(clearBeforeNum));
        mongoTemplate.remove(query, XxlJobLog.class);
    }


    @Override
    public void clearLog(Long[] jobLogIds) {
        if(jobLogIds == null || jobLogIds.length == 0)
            return;
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").in(jobLogIds));
        mongoTemplate.remove(query, XxlJobLog.class);
    }
}
