package me.izhong.jobs.manage.impl.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.service.CrudBaseServiceImpl;
import com.mongodb.client.result.UpdateResult;
import me.izhong.jobs.manage.impl.core.model.ZJobLog;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
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

@Slf4j
@Service
public class ZJobLogServiceImpl extends CrudBaseServiceImpl<Long,ZJobLog> implements ZJobLogService {
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
        List<ZJobLog> ls = super.selectList(query,null,null);
        if(ls ==null || ls.size() == 0)
            return new ArrayList<>();
        return ls.stream().map(e->e.getJobId()).collect(Collectors.toList());

    }

    @Override
    public List<ZJobLog> findRunningJobs() {
        Query query = new Query();
        query.addCriteria(Criteria.where("handleCode").is(null));
        return super.selectList(query,null,null);
    }

    @Override
    public List<ZJobLog> findJobLogByJobId(Long jobId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobId").is(jobId));
        return super.selectList(query,null,null);
    }

    @Transactional
    @Override
    public ZJobLog insertTriggerBeginMessage(Long jobId, Long jobGroupId, String jobDesc, Date triggerTime, Integer finalFailRetryCount) {
        ZJobLog jobLog = new ZJobLog();
        jobLog.setJobId(jobId);
        jobLog.setJobGroupId(jobGroupId);
        jobLog.setJobDesc(jobDesc);
        jobLog.setTriggerTime(triggerTime);
        jobLog.setExecutorFailRetryCount(finalFailRetryCount);
        return super.insert(jobLog);
    }

    @Transactional
    @Override
    public void updateTriggerDoneMessage(Long jobLogId, String executorHandler,
                                         String executorParam, Integer triggerCode, String triggerMsg) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);
        options.returnNew(true);
        Update update = new Update();
        if(executorHandler !=null)
            update.set("executorHandler",executorHandler);
        if(executorParam != null)
            update.set("executorParam",executorParam);
        update.set("triggerCode",triggerCode);
        update.set("triggerMsg",triggerMsg);
        ZJobLog ur = mongoTemplate.findAndModify(query, update, options, ZJobLog.class);
        //log.info("返回新值:{},{}",ur.getTriggerCode(),ur.getTriggerMsg());
    }

    @Override
    public void updateHandleStartMessage(Long jobLogId, Date startTime) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        Update update = new Update();
        update.set("handleTime",startTime);
        mongoTemplate.findAndModify(query, update, ZJobLog.class);
    }

    @Override
    public void updateHandleDoneMessage(Long jobLogId, Integer handleCode, String handleMsg) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        Update update = new Update();
        update.set("handleCode",handleCode);
        update.set("handleMsg",handleMsg);
        mongoTemplate.findAndModify(query, update, ZJobLog.class);
    }

    @Override
    public long updateAlarmStatus(long failLogId, int oldStatus, int newStatus) {
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(failLogId));
        query.addCriteria(Criteria.where("alarmStatus").is(oldStatus));

        Update update = new Update();
        update.set("alarmStatus",newStatus);
        UpdateResult ur = mongoTemplate.updateMulti(query, update, ZJobLog.class);
        return ur.getModifiedCount();
    }

    @Transactional
    @Override
    public void updateExecutorAddress(Long jobLogId, String address) {
        Assert.notNull(jobLogId,"");
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").is(jobLogId));

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.upsert(true);

        Update update = new Update();
        update.set("executorAddress",address);
        mongoTemplate.findAndModify(query, update, options,ZJobLog.class);
    }


    public ZJobLog update(ZJobLog target) {
        log.info("targetAddress:{}",target.getExecutorAddress());
        return super.update(target);
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
        mongoTemplate.remove(query, ZJobLog.class);
    }


    @Override
    public void clearLog(Long[] jobLogIds) {
        if(jobLogIds == null || jobLogIds.length == 0)
            return;
        Query query = new Query();
        query.addCriteria(Criteria.where("jobLogId").in(jobLogIds));
        mongoTemplate.remove(query, ZJobLog.class);
    }
}
