package me.izhong.jobs.manage.impl.service.impl;

import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import me.izhong.db.common.service.CrudBaseServiceImpl;
import me.izhong.domain.PageModel;
import me.izhong.domain.PageRequest;
import me.izhong.jobs.manage.impl.core.cron.CronExpression;
import me.izhong.jobs.manage.impl.core.model.ZJobGroup;
import me.izhong.jobs.manage.impl.core.model.ZJobInfo;
import me.izhong.jobs.manage.impl.core.thread.JobScheduleHelper;
import me.izhong.jobs.manage.impl.service.ZJobGroupService;
import me.izhong.jobs.manage.impl.service.ZJobInfoService;
import me.izhong.jobs.manage.impl.service.ZJobScriptService;
import me.izhong.jobs.manage.impl.service.ZJobLogService;
import me.izhong.jobs.type.ExecutorBlockStrategyEnum;
import me.izhong.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

@Slf4j
@Service
public class ZJobInfoServiceImpl extends CrudBaseServiceImpl<Long,ZJobInfo> implements ZJobInfoService {
	private static Logger logger = LoggerFactory.getLogger(ZJobInfoServiceImpl.class);

	@Resource
	private ZJobGroupService xxlJobGroupService;
	@Resource
	private ZJobInfoService zJobInfoService;
	@Resource
	public ZJobLogService zJobLogService;
	@Resource
	private ZJobScriptService zJobScriptService;

	@Override
	public List<ZJobInfo> scheduleJobQuery(long maxNextTime) {
		Query query = new Query();
		query.addCriteria(Criteria.where("triggerNextTime").lte(maxNextTime));
		query.addCriteria(Criteria.where("triggerStatus").is(0));
		return super.selectList(query, null, null);
	}

	@Transactional
	@Override
	public void scheduleUpdate(ZJobInfo jobInfo) {
		Assert.notNull(jobInfo,"");
		Assert.notNull(jobInfo.getJobId(),"");
		Assert.notNull(jobInfo.getTriggerLastTime(),"");
		Assert.notNull(jobInfo.getTriggerNextTime(),"");
		Assert.notNull(jobInfo.getTriggerStatus(),"");

		Query query = new Query();
		query.addCriteria(Criteria.where("jobId").is(jobInfo.getJobId()));

		Update update = new Update();
		update.set("triggerLastTime",jobInfo.getTriggerLastTime());
		update.set("triggerNextTime",jobInfo.getTriggerNextTime());
		update.set("triggerStatus",jobInfo.getTriggerStatus());
		UpdateResult ur = mongoTemplate.updateMulti(query, update, ZJobInfo.class);
		if(ur.getModifiedCount() != 1) {
			log.error("更新数量异常,数量:{}" , ur.getModifiedCount());
		}
	}

	@Override
	public void updateWaitAgain(Long jobId, Boolean waitAgain) {
		Assert.notNull(jobId,"");
		Assert.notNull(waitAgain,"");

		Query query = new Query();
		query.addCriteria(Criteria.where("jobId").is(jobId));

		Update update = new Update();
		update.set("wakeAgain",waitAgain);
		UpdateResult ur = mongoTemplate.updateMulti(query, update, ZJobInfo.class);
		if(ur.getModifiedCount() != 1) {
			log.error("更新数量异常,数量:{}" , ur.getModifiedCount());
		}
	}

	@Transactional
	@Override
	public void updateRunningTriggers(Long jobId, List<Long> runningTriggerIds) {
		Assert.notNull(jobId,"");
		Assert.notNull(runningTriggerIds,"");

		Query query = new Query();
		query.addCriteria(Criteria.where("jobId").is(jobId));

		Update update = new Update();
		//update.set("runningCount",runningCount);
		update.set("runningTriggerIds",runningTriggerIds);
		UpdateResult ur = mongoTemplate.updateMulti(query, update, ZJobInfo.class);
		if(ur.getModifiedCount() != 1) {
			log.error("更新数量异常,数量:{}" , ur.getModifiedCount());
		}
	}

	@Override
	public void updateJobScriptId(Long jobId, Long jobScriptId) {
		Assert.notNull(jobId,"");
		Assert.notNull(jobScriptId,"");

		Query query = new Query();
		query.addCriteria(Criteria.where("jobId").is(jobId));

		Update update = new Update();
		update.set("jobScriptId",jobScriptId);
		UpdateResult ur = mongoTemplate.updateMulti(query, update, ZJobInfo.class);
		if(ur.getModifiedCount() != 1) {
			log.error("更新数量异常,数量:{}" , ur.getModifiedCount());
		}
	}

	@Override
	public PageModel<ZJobInfo> pageList(PageRequest request, ZJobInfo jobInfo){
	//public Map<String, Object> pageList(int start, int length, int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {

		// page list
//		List<ZJobInfo> list = xxlJobInfoDao.pageList(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
//		int list_count = xxlJobInfoDao.pageListCount(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
//
//		// package result
//		Map<String, Object> maps = new HashMap<String, Object>();
//	    maps.put("recordsTotal", list_count);		// 总记录数
//	    maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
//	    maps.put("data", list);  					// 分页列表
//		return maps;
		return zJobInfoService.selectPage(request,jobInfo);
	}

	@Override
	public List<ZJobInfo> findRunningJobs() {
		Query query = new Query();

		//Criteria cr = new Criteria();
		//cr.orOperator(Criteria.where("runningTriggerIds").ne(0).gt(0), Criteria.where("runningTriggerIds").is(null));

		query.addCriteria(Criteria.where("runningTriggerIds").ne(Collections.EMPTY_LIST));

		return super.selectList(query,null,null);	}

	@Transactional
	@Override
	public ReturnT<String> addJob(ZJobInfo jobInfo) {
		// valid
		if(jobInfo.getJobGroupId() == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "任务组必填" );
		}
		ZJobGroup group = xxlJobGroupService.selectByPId(jobInfo.getJobGroupId());
		if (group == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "分组不存在");
		}
		if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "Cron表达式必填" );
		}
		if (jobInfo.getJobDesc()==null || jobInfo.getJobDesc().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "描述必填" );
		}
		if (jobInfo.getAuthor()==null || jobInfo.getAuthor().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "作者必填");
		}
//		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
//			return new ReturnT<String>(ReturnT.FAIL_CODE, "路由策略必填" );
//		}
		if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "阻塞策略不能为空" );
		}

		// ChildJobId valid
		if (jobInfo.getChildJobId()!=null && jobInfo.getChildJobId().trim().length()>0) {
			String[] childJobIds = jobInfo.getChildJobId().split(",");
			for (String childJobIdItem: childJobIds) {
				if (childJobIdItem!=null && childJobIdItem.trim().length()>0 && isNumeric(childJobIdItem)) {
					ZJobInfo childJobInfo = selectByPId(Long.valueOf(childJobIdItem));
					if (childJobInfo==null) {
						return new ReturnT<String>(ReturnT.FAIL_CODE,
								MessageFormat.format("子任务不存在 {}", childJobIdItem));
					}
				} else {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format("子任务异常 {}", childJobIdItem));
				}
			}

			String temp = "";	// join ,
			for (String item:childJobIds) {
				temp += item + ",";
			}
			temp = temp.substring(0, temp.length()-1);

			jobInfo.setChildJobId(temp);
		}

        jobInfo.setCreateTime(new Date());
		jobInfo.setUpdateTime(new Date());
		// addJobScript in db
		zJobInfoService.insert(jobInfo);
		if (jobInfo.getJobId() < 1) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "插入异常" );
		}

		return new ReturnT<String>(String.valueOf(jobInfo.getJobId()));
	}

	private boolean isNumeric(String str){
		try {
			int result = Integer.valueOf(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Transactional
	@Override
	public ReturnT<String> updateJob(ZJobInfo jobInfo) {

		Assert.notNull(jobInfo,"");
		Assert.notNull(jobInfo.getJobId(),"任务ID不能为空");
		log.info("update jobInfo JobDesc{}",jobInfo.getJobDesc());
		// valid
		if(jobInfo.getJobGroupId() == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "任务组必填" );
		}
		ZJobGroup group = xxlJobGroupService.selectByPId(jobInfo.getJobGroupId());
		if (group == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "任务组不存在" );
		}
		if (!CronExpression.isValidExpression(jobInfo.getJobCron())) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "cron表达式非法");
		}
		if (jobInfo.getJobDesc()==null || jobInfo.getJobDesc().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "任务描述不能为空");
		}
		if (jobInfo.getAuthor()==null || jobInfo.getAuthor().trim().length()==0) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "作者不能为空");
		}
//		if (ExecutorRouteStrategyEnum.match(jobInfo.getExecutorRouteStrategy(), null) == null) {
//			return new ReturnT<String>(ReturnT.FAIL_CODE, "路由策略不能为空" );
//		}
		if (ExecutorBlockStrategyEnum.match(jobInfo.getExecutorBlockStrategy(), null) == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "阻塞策略不能为空" );
		}

		// ChildJobId valid
		if (jobInfo.getChildJobId()!=null && jobInfo.getChildJobId().trim().length()>0) {
			String[] childJobIds = jobInfo.getChildJobId().split(",");
			for (String childJobIdItem: childJobIds) {
				if (childJobIdItem!=null && childJobIdItem.trim().length()>0 && isNumeric(childJobIdItem)) {
					ZJobInfo childJobInfo = selectByPId(Long.valueOf(childJobIdItem));
					if (childJobInfo==null) {
						return new ReturnT<String>(ReturnT.FAIL_CODE,
								MessageFormat.format("子任务不存在 {}", childJobIdItem));
					}
				} else {
					return new ReturnT<String>(ReturnT.FAIL_CODE,
							MessageFormat.format("子任务异常 {}", childJobIdItem));
				}
			}

			String temp = "";	// join ,
			for (String item:childJobIds) {
				temp += item + ",";
			}
			temp = temp.substring(0, temp.length()-1);

			jobInfo.setChildJobId(temp);
		}

		// stage job info
		ZJobInfo exists_jobInfo = zJobInfoService.selectByPId(jobInfo.getJobId());
		if (exists_jobInfo == null) {
			return new ReturnT<String>(ReturnT.FAIL_CODE, "任务不存在");
		}

		// next trigger time (5s后生效，避开预读周期)
		Long nextTriggerTime = exists_jobInfo.getTriggerNextTime();
		//if (Integer.valueOf(1).equals(exists_jobInfo.getTriggerStatus()) && !jobInfo.getJobCron().equals(exists_jobInfo.getJobCron()) ) {
			try {
				Date nextValidTime = new CronExpression(jobInfo.getJobCron()).getNextValidTimeAfter(new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
				if (nextValidTime == null) {
					return new ReturnT<String>(ReturnT.FAIL_CODE,"任务永远不会触发");
				}
				nextTriggerTime = nextValidTime.getTime();
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
				return new ReturnT<String>(ReturnT.FAIL_CODE, " 任务表达式非法"+ e.getMessage());
			}
		//}

		exists_jobInfo.setJobGroupId(jobInfo.getJobGroupId());
		exists_jobInfo.setJobCron(jobInfo.getJobCron());
		exists_jobInfo.setJobDesc(jobInfo.getJobDesc());
		exists_jobInfo.setAuthor(jobInfo.getAuthor());
		exists_jobInfo.setAlarmEmail(jobInfo.getAlarmEmail());
		exists_jobInfo.setExecutorRouteStrategy(jobInfo.getExecutorRouteStrategy());
		exists_jobInfo.setExecutorHandler(jobInfo.getExecutorHandler());
		exists_jobInfo.setExecutorParam(jobInfo.getExecutorParam());
		exists_jobInfo.setExecutorBlockStrategy(jobInfo.getExecutorBlockStrategy());
		exists_jobInfo.setExecutorTimeout(jobInfo.getExecutorTimeout());
		exists_jobInfo.setExecutorFailRetryCount(jobInfo.getExecutorFailRetryCount());
		exists_jobInfo.setChildJobId(jobInfo.getChildJobId());
		exists_jobInfo.setTriggerNextTime(nextTriggerTime);

		exists_jobInfo.setTriggerStatus(jobInfo.getTriggerStatus());
		exists_jobInfo.setRemark(jobInfo.getRemark());

        zJobInfoService.update(exists_jobInfo);

		return ReturnT.SUCCESS;
	}

	@Transactional
	@Override
	public ReturnT<String> removeJob(long id) {
		ZJobInfo zJobInfo = zJobInfoService.selectByPId(id);
		if (zJobInfo == null) {
			return ReturnT.SUCCESS;
		}

		zJobInfoService.deleteByPId(id);
		zJobLogService.deleteByPId(id);
		zJobScriptService.deleteByPId(id);
		return ReturnT.SUCCESS;
	}

	@Transactional
	@Override
	public ReturnT<String> enableJob(long id) {
		ZJobInfo zJobInfo = zJobInfoService.selectByPId(id);

		// next trigger time (5s后生效，避开预读周期)
		long nextTriggerTime = 0;
		try {
			Date nextValidTime = new CronExpression(zJobInfo.getJobCron()).getNextValidTimeAfter(new Date(System.currentTimeMillis() + JobScheduleHelper.PRE_READ_MS));
			if (nextValidTime == null) {
				return new ReturnT<String>(ReturnT.FAIL_CODE, "任务永远不会触发");
			}
			nextTriggerTime = nextValidTime.getTime();
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<String>(ReturnT.FAIL_CODE, "表达式非法"+ e.getMessage());
		}

		zJobInfo.setTriggerStatus(0L);
		zJobInfo.setTriggerLastTime(0L);
		zJobInfo.setTriggerNextTime(nextTriggerTime);

		zJobInfoService.update(zJobInfo);
		return ReturnT.SUCCESS;
	}

	@Transactional
	@Override
	public ReturnT<String> disableJob(long id) {
        ZJobInfo zJobInfo = zJobInfoService.selectByPId(id);

		zJobInfo.setTriggerStatus(1L);
		zJobInfo.setTriggerLastTime(0L);
		zJobInfo.setTriggerNextTime(0L);

		zJobInfoService.update(zJobInfo);
		return ReturnT.SUCCESS;
	}

	@Override
	public Map<String, Object> dashboardInfo() {

		long jobInfoCount = zJobInfoService.count();
		long jobLogCount = zJobLogService.count();
		long jobLogSuccessCount = zJobLogService.triggerCountByHandleCode(ReturnT.SUCCESS_CODE);

		// executor count
		Set<String> executerAddressSet = new HashSet<String>();
		List<ZJobGroup> groupList = xxlJobGroupService.selectAll();

		if (groupList!=null && !groupList.isEmpty()) {
			for (ZJobGroup group: groupList) {
				if (group.getRegistryList()!=null && !group.getRegistryList().isEmpty()) {
					executerAddressSet.addAll(group.getRegistryList());
				}
			}
		}

		int executorCount = executerAddressSet.size();

		Map<String, Object> dashboardMap = new HashMap<String, Object>();
		dashboardMap.put("jobInfoCount", jobInfoCount);
		dashboardMap.put("jobLogCount", jobLogCount);
		dashboardMap.put("jobLogSuccessCount", jobLogSuccessCount);
		dashboardMap.put("executorCount", executorCount);
		return dashboardMap;
	}

	private static final String TRIGGER_CHART_DATA_CACHE = "trigger_chart_data_cache";
	@Override
	public ReturnT<Map<String, Object>> chartInfo(Date startDate, Date endDate) {
		/*// get cache
		String cacheKey = TRIGGER_CHART_DATA_CACHE + "_" + startDate.getTime() + "_" + endDate.getTime();
		Map<String, Object> chartInfo = (Map<String, Object>) LocalCacheUtil.get(cacheKey);
		if (chartInfo != null) {
			return new ReturnT<Map<String, Object>>(chartInfo);
		}*/

		// process
		List<String> triggerDayList = new ArrayList<String>();
		List<Integer> triggerDayCountRunningList = new ArrayList<Integer>();
		List<Integer> triggerDayCountSucList = new ArrayList<Integer>();
		List<Integer> triggerDayCountFailList = new ArrayList<Integer>();
		int triggerCountRunningTotal = 0;
		int triggerCountSucTotal = 0;
		int triggerCountFailTotal = 0;

//		List<Map<String, Object>> triggerCountMapAll = zJobLogService.triggerCountByDay(startDate, endDate);
//		if (triggerCountMapAll!=null && triggerCountMapAll.size()>0) {
//			for (Map<String, Object> item: triggerCountMapAll) {
//				String day = String.valueOf(item.get("triggerDay"));
//				int triggerDayCount = Integer.valueOf(String.valueOf(item.get("triggerDayCount")));
//				int triggerDayCountRunning = Integer.valueOf(String.valueOf(item.get("triggerDayCountRunning")));
//				int triggerDayCountSuc = Integer.valueOf(String.valueOf(item.get("triggerDayCountSuc")));
//				int triggerDayCountFail = triggerDayCount - triggerDayCountRunning - triggerDayCountSuc;
//
//				triggerDayList.addJobScript(day);
//				triggerDayCountRunningList.addJobScript(triggerDayCountRunning);
//				triggerDayCountSucList.addJobScript(triggerDayCountSuc);
//				triggerDayCountFailList.addJobScript(triggerDayCountFail);
//
//				triggerCountRunningTotal += triggerDayCountRunning;
//				triggerCountSucTotal += triggerDayCountSuc;
//				triggerCountFailTotal += triggerDayCountFail;
//			}
//		} else {
//            for (int i = 4; i > -1; i--) {
//                triggerDayList.addJobScript(DateUtil.formatDate(DateUtil.addDays(new Date(), -i)));
//				triggerDayCountRunningList.addJobScript(0);
//                triggerDayCountSucList.addJobScript(0);
//                triggerDayCountFailList.addJobScript(0);
//            }
//		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("triggerDayList", triggerDayList);
		result.put("triggerDayCountRunningList", triggerDayCountRunningList);
		result.put("triggerDayCountSucList", triggerDayCountSucList);
		result.put("triggerDayCountFailList", triggerDayCountFailList);

		result.put("triggerCountRunningTotal", triggerCountRunningTotal);
		result.put("triggerCountSucTotal", triggerCountSucTotal);
		result.put("triggerCountFailTotal", triggerCountFailTotal);

		/*// set cache
		LocalCacheUtil.set(cacheKey, result, 60*1000);     // cache 60s*/

		return new ReturnT<Map<String, Object>>(result);
	}

}
